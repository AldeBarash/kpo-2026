package studying.gateway.service;

import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import studying.gateway.dto.ChatCompletionRequest;
import studying.gateway.dto.ChatCompletionResponse;

@Service
public class ChatCompletionService {

    private final ChatClient chatClient;
    private final BillingService billingService;
    private final HashingService hashingService;

    public ChatCompletionService(ChatClient.Builder chatClientBuilder,
                                 BillingService billingService,
                                 HashingService hashingService) {
        this.chatClient = chatClientBuilder.build();
        this.billingService = billingService;
        this.hashingService = hashingService;
    }

    public ChatCompletionResponse complete(String authorization,
                                           String idempotencyKey,
                                           ChatCompletionRequest command) {
        String apiKey = bearerToken(authorization);
        String requestHash = hashingService.sha256(command.model() + "\n"
                + command.maxTokens() + "\n" + command.prompt());
        BillingService.Reservation reservation = billingService.reserve(
                apiKey, idempotencyKey, requestHash, command.model(),
                command.prompt(), command.maxTokens());
        if (reservation.cached()) {
            return response(reservation.requestId(), reservation.modelName(),
                    reservation.cachedContent(), reservation.cachedPromptTokens(),
                    reservation.cachedCompletionTokens(), reservation.cachedCost(),
                    reservation.cachedBalance());
        }

        try {
            ChatResponse aiResponse = chatClient.prompt()
                    .user(command.prompt())
                    .options(OllamaChatOptions.builder()
                            .model(command.model())
                            .numPredict(command.maxTokens()))
                    .call()
                    .chatResponse();
            if (aiResponse == null || aiResponse.getResult() == null) {
                throw new IllegalStateException("Ollama returned an empty response");
            }
            Usage usage = aiResponse.getMetadata().getUsage();
            if (usage == null || usage.getTotalTokens() == null
                    || usage.getTotalTokens() <= 0) {
                throw new IllegalStateException("Ollama did not return token usage");
            }
            String content = aiResponse.getResult().getOutput().getText();
            BillingService.CompletedCall completed = billingService.complete(
                    reservation.requestId(), usage.getPromptTokens(),
                    usage.getCompletionTokens(), content);
            return response(completed.requestId(), completed.modelName(),
                    completed.content(), completed.promptTokens(),
                    completed.completionTokens(), completed.cost(), completed.balance());
        } catch (Exception exception) {
            billingService.fail(reservation.requestId(), "OLLAMA_ERROR");
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "OLLAMA_UNAVAILABLE",
                    "Не удалось получить ответ локальной модели");
        }
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY",
                    "Передайте API-ключ в заголовке Authorization: Bearer ...");
        }
        return authorization.substring(7).strip();
    }

    private ChatCompletionResponse response(UUID requestId, String model,
                                            String content, int promptTokens,
                                            int completionTokens, long cost,
                                            long balance) {
        return new ChatCompletionResponse(requestId, model, content,
                new ChatCompletionResponse.UsageResponse(
                        promptTokens, completionTokens,
                        Math.addExact(promptTokens, completionTokens), cost),
                balance);
    }
}
