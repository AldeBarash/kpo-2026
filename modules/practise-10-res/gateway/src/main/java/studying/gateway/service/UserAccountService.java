package studying.gateway.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studying.gateway.domain.ApiKeyEntity;
import studying.gateway.domain.UserEntity;
import studying.gateway.domain.UserStatus;
import studying.gateway.domain.WalletEntity;
import studying.gateway.domain.WalletTransactionEntity;
import studying.gateway.domain.WalletTransactionType;
import studying.gateway.repository.ApiKeyRepository;
import studying.gateway.repository.UserRepository;
import studying.gateway.repository.WalletRepository;
import studying.gateway.repository.WalletTransactionRepository;
import studying.gateway.dto.ApiKeyResponse;
import studying.gateway.dto.UserResponse;
import studying.gateway.dto.WalletResponse;
import studying.gateway.dto.WalletTransactionResponse;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final HashingService hashingService;

    @Transactional
    public UserResponse register(String rawEmail) {
        String email = rawEmail.strip().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                    "Пользователь с таким email уже существует");
        }
        UserEntity user = userRepository.save(new UserEntity(
                UUID.randomUUID(), email, UserStatus.ACTIVE, Instant.now()));
        walletRepository.save(new WalletEntity(UUID.randomUUID(), user));
        return new UserResponse(user.getId(), user.getEmail(), 0, 0);
    }

    @Transactional
    public WalletResponse topUp(UUID userId, long amount) {
        if (amount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOP_UP",
                    "Сумма пополнения должна быть положительной");
        }
        WalletEntity wallet = lockedWallet(userId);
        try {
            wallet.topUp(amount);
        } catch (ArithmeticException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BALANCE_OVERFLOW",
                    "Сумма пополнения слишком велика");
        }
        transactionRepository.save(new WalletTransactionEntity(
                UUID.randomUUID(), wallet, null, WalletTransactionType.TOP_UP,
                amount, Instant.now()));
        return walletResponse(userId, wallet);
    }

    @Transactional
    public ApiKeyResponse issueApiKey(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .filter(value -> value.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> notFound("USER_NOT_FOUND", "Пользователь не найден"));
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String key = "sk-local-" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
        String prefix = key.substring(0, Math.min(16, key.length()));
        ApiKeyEntity entity = apiKeyRepository.save(new ApiKeyEntity(
                UUID.randomUUID(), user, prefix, hashingService.sha256(key), Instant.now()));
        return new ApiKeyResponse(entity.getId(), key, prefix);
    }

    @Transactional(readOnly = true)
    public WalletResponse wallet(UUID userId) {
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("WALLET_NOT_FOUND", "Кошелёк не найден"));
        return walletResponse(userId, wallet);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> transactions(UUID userId, int page, int size) {
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("WALLET_NOT_FOUND", "Кошелёк не найден"));
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
        return transactionRepository.findByWalletId(wallet.getId(), pageable)
                .map(transaction -> new WalletTransactionResponse(
                        transaction.getId(), transaction.getType(), transaction.getAmount(),
                        transaction.getBalanceAfter(), transaction.getReservedAfter(),
                        transaction.getCreatedAt()));
    }

    private WalletEntity lockedWallet(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> notFound("WALLET_NOT_FOUND", "Кошелёк не найден"));
    }

    private WalletResponse walletResponse(UUID userId, WalletEntity wallet) {
        return new WalletResponse(userId, wallet.getBalance(), wallet.getReservedBalance(),
                wallet.availableBalance());
    }

    private ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }
}
