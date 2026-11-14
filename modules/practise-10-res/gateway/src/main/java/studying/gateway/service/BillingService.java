package studying.gateway.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studying.gateway.domain.AiModelEntity;
import studying.gateway.domain.AiRequestEntity;
import studying.gateway.domain.AiRequestStatus;
import studying.gateway.domain.ApiKeyEntity;
import studying.gateway.domain.ApiKeyStatus;
import studying.gateway.domain.ModelPriceEntity;
import studying.gateway.domain.ModelStatus;
import studying.gateway.domain.UsageRecordEntity;
import studying.gateway.domain.UserStatus;
import studying.gateway.domain.WalletEntity;
import studying.gateway.domain.WalletTransactionEntity;
import studying.gateway.domain.WalletTransactionType;
import studying.gateway.repository.AiModelRepository;
import studying.gateway.repository.AiRequestRepository;
import studying.gateway.repository.ApiKeyRepository;
import studying.gateway.repository.ModelPriceRepository;
import studying.gateway.repository.UsageRecordRepository;
import studying.gateway.repository.WalletRepository;
import studying.gateway.repository.WalletTransactionRepository;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final ApiKeyRepository apiKeyRepository;
    private final AiModelRepository modelRepository;
    private final ModelPriceRepository priceRepository;
    private final WalletRepository walletRepository;
    private final AiRequestRepository requestRepository;
    private final UsageRecordRepository usageRepository;
    private final WalletTransactionRepository transactionRepository;
    private final HashingService hashingService;
    private final studying.gateway.events.OutboxWriter outbox;

    @Transactional
    public Reservation reserve(String rawApiKey, String idempotencyKey,
                               String requestHash, String modelName,
                               String prompt, int maxTokens) {
        Instant now = Instant.now();
        ApiKeyEntity apiKey = apiKeyRepository
                .findByKeyHashAndStatus(hashingService.sha256(rawApiKey), ApiKeyStatus.ACTIVE)
                .filter(value -> value.isUsable(now))
                .filter(value -> value.getUser().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "INVALID_API_KEY", "API-ключ недействителен"));
        apiKey.markUsed(now);

        var existing = requestRepository.findByUserIdAndIdempotencyKey(
                apiKey.getUser().getId(), idempotencyKey);
        if (existing.isPresent()) {
            return existingReservation(existing.get(), requestHash);
        }

        AiModelEntity model = modelRepository.findByNameAndStatus(modelName, ModelStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "MODEL_NOT_FOUND", "Модель не найдена"));
        ModelPriceEntity price = priceRepository.findCurrent(model.getId(), now)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                        "MODEL_PRICE_NOT_FOUND", "Для модели не задан действующий тариф"));

        int inputUpperBound = Math.addExact(
                prompt.getBytes(StandardCharsets.UTF_8).length, 128);
        long reserve = Math.addExact(
                cost(inputUpperBound, price.getInputPricePer1000()),
                cost(maxTokens, price.getOutputPricePer1000()));

        WalletEntity wallet = walletRepository.findByUserIdForUpdate(apiKey.getUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "WALLET_NOT_FOUND", "Кошелёк не найден"));
        if (wallet.availableBalance() < reserve) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED,
                    "INSUFFICIENT_CREDITS", "Недостаточно кредитов");
        }
        wallet.reserve(reserve);

        AiRequestEntity request = requestRepository.save(new AiRequestEntity(
                UUID.randomUUID(), apiKey.getUser(), apiKey, model, price,
                idempotencyKey, requestHash, maxTokens, reserve, now));
        transactionRepository.save(new WalletTransactionEntity(
                UUID.randomUUID(), wallet, request, WalletTransactionType.RESERVE,
                -reserve, now));
        return Reservation.newCall(request.getId(), model.getName(), maxTokens, reserve);
    }

    @Transactional
    public CompletedCall complete(UUID requestId, int promptTokens,
                                  int completionTokens, String content) {
        Instant now = Instant.now();
        AiRequestEntity request = processingRequest(requestId);
        long actualCost = Math.addExact(
                cost(promptTokens, request.getModelPrice().getInputPricePer1000()),
                cost(completionTokens, request.getModelPrice().getOutputPricePer1000()));
        if (actualCost > request.getReservedCredits()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "RESERVE_TOO_SMALL", "Фактическая стоимость превысила резерв");
        }

        WalletEntity wallet = walletRepository.findByUserIdForUpdate(request.getUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "WALLET_NOT_FOUND", "Кошелёк не найден"));
        wallet.charge(request.getReservedCredits(), actualCost);
        request.complete(actualCost, content, now);
        usageRepository.save(new UsageRecordEntity(
                UUID.randomUUID(), request, promptTokens, completionTokens,
                request.getModel().getName(), now));
        transactionRepository.save(new WalletTransactionEntity(
                UUID.randomUUID(), wallet, request, WalletTransactionType.CHARGE,
                -actualCost, now));
        long released = request.getReservedCredits() - actualCost;
        if (released > 0) {
            transactionRepository.save(new WalletTransactionEntity(
                    UUID.randomUUID(), wallet, request, WalletTransactionType.RELEASE,
                    released, now));
        }
        outbox.append(new studying.gateway.events.CreditsCharged(
                UUID.randomUUID(), "CreditsCharged", 1, now, request.getUser().getId(),
                request.getId(), request.getModel().getName(), promptTokens, completionTokens,
                actualCost, wallet.getBalance()));
        return new CompletedCall(request.getId(), request.getModel().getName(), content,
                promptTokens, completionTokens, actualCost, wallet.getBalance());
    }

    @Transactional
    public void fail(UUID requestId, String errorCode) {
        AiRequestEntity request = requestRepository.findById(requestId).orElse(null);
        if (request == null || request.getStatus() != AiRequestStatus.PROCESSING) {
            return;
        }
        Instant now = Instant.now();
        WalletEntity wallet = walletRepository.findByUserIdForUpdate(request.getUser().getId())
                .orElseThrow();
        wallet.release(request.getReservedCredits());
        request.fail(errorCode, now);
        transactionRepository.save(new WalletTransactionEntity(
                UUID.randomUUID(), wallet, request, WalletTransactionType.RELEASE,
                request.getReservedCredits(), now));
    }

    private Reservation existingReservation(AiRequestEntity request, String requestHash) {
        if (!request.getRequestHash().equals(requestHash)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                    "Idempotency-Key уже использован для другого запроса");
        }
        if (request.getStatus() != AiRequestStatus.COMPLETED) {
            throw new ApiException(HttpStatus.CONFLICT, "REQUEST_ALREADY_IN_PROGRESS",
                    "Запрос с таким Idempotency-Key уже выполняется или завершился ошибкой");
        }
        UsageRecordEntity usage = usageRepository.findByAiRequestId(request.getId())
                .orElseThrow();
        WalletEntity wallet = walletRepository.findByUserId(request.getUser().getId())
                .orElseThrow();
        return Reservation.cached(request.getId(), request.getModel().getName(),
                request.getResponseContent(), usage.getPromptTokens(),
                usage.getCompletionTokens(), request.getActualCost(), wallet.getBalance());
    }

    private AiRequestEntity processingRequest(UUID requestId) {
        return requestRepository.findById(requestId)
                .filter(value -> value.getStatus() == AiRequestStatus.PROCESSING)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                        "REQUEST_NOT_PROCESSING", "Запрос уже завершён"));
    }

    private long cost(int tokens, long pricePer1000) {
        if (tokens < 0) {
            throw new IllegalArgumentException("Token count must not be negative");
        }
        long product = Math.multiplyExact((long) tokens, pricePer1000);
        return Math.floorDiv(Math.addExact(product, 999), 1000);
    }

    public record Reservation(
            UUID requestId,
            String modelName,
            int maxTokens,
            long reservedCredits,
            boolean cached,
            String cachedContent,
            int cachedPromptTokens,
            int cachedCompletionTokens,
            long cachedCost,
            long cachedBalance
    ) {
        static Reservation newCall(UUID id, String model, int maxTokens, long reserve) {
            return new Reservation(id, model, maxTokens, reserve, false,
                    null, 0, 0, 0, 0);
        }

        static Reservation cached(UUID id, String model, String content,
                                  int promptTokens, int completionTokens,
                                  long cost, long balance) {
            return new Reservation(id, model, 0, 0, true, content,
                    promptTokens, completionTokens, cost, balance);
        }
    }

    public record CompletedCall(
            UUID requestId,
            String modelName,
            String content,
            int promptTokens,
            int completionTokens,
            long cost,
            long balance
    ) {
    }
}
