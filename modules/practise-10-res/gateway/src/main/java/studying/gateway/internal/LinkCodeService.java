package studying.gateway.internal;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studying.gateway.domain.ApiKeyStatus;
import studying.gateway.domain.UserStatus;
import studying.gateway.repository.ApiKeyRepository;
import studying.gateway.service.ApiException;
import studying.gateway.service.HashingService;

@Service
@RequiredArgsConstructor
public class LinkCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final ApiKeyRepository keys;
    private final HashingService hashes;

    public record LinkCode(String code, Instant expiresAt) { }
    public record Redemption(long updateId, long telegramUserId, long chatId, String code) { }
    public record Redeemed(UUID userId) { }

    @Transactional
    public LinkCode createCode(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) { throw unauthorized(); }
        var key = keys.findByKeyHashAndStatus(hashes.sha256(authorization.substring(7).strip()), ApiKeyStatus.ACTIVE)
                .filter(value -> value.isUsable(Instant.now()))
                .filter(value -> value.getUser().getStatus() == UserStatus.ACTIVE)
                .orElseThrow(this::unauthorized);
        UUID user = key.getUser().getId();
        jdbc.queryForList("SELECT id FROM users WHERE id = ? FOR UPDATE", user);
        jdbc.update("DELETE FROM telegram_link_codes WHERE user_id = ?", user);
        byte[] bytes = new byte[24]; RANDOM.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expires = Instant.now().plusSeconds(300);
        jdbc.update("INSERT INTO telegram_link_codes (code_hash, user_id, expires_at) VALUES (?, ?, ?)",
                hashes.sha256(code), user, Timestamp.from(expires));
        return new LinkCode(code, expires);
    }

    @Transactional
    public Redeemed redeem(Redemption request) {
        if (request.updateId() < 0 || request.telegramUserId() <= 0 || request.chatId() != request.telegramUserId()
                || request.code() == null || request.code().isBlank() || request.code().length() > 128) {
            throw invalidCode();
        }
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)", request.updateId());
        String hash = hashes.sha256(request.code());
        var receipts = jdbc.queryForList("SELECT * FROM telegram_link_redemptions WHERE update_id = ?", request.updateId());
        if (!receipts.isEmpty()) {
            var receipt = receipts.getFirst();
            if (!hash.equals(receipt.get("code_hash"))
                    || ((Number) receipt.get("telegram_user_id")).longValue() != request.telegramUserId()
                    || ((Number) receipt.get("chat_id")).longValue() != request.chatId()) {
                throw new ApiException(HttpStatus.CONFLICT, "REDEMPTION_CONFLICT", "Операция уже использована");
            }
            return new Redeemed((UUID) receipt.get("user_id"));
        }
        var users = jdbc.queryForList("SELECT user_id FROM telegram_link_codes WHERE code_hash = ?", UUID.class, hash);
        if (users.isEmpty()) { throw invalidCode(); }
        UUID user = users.getFirst();
        jdbc.queryForList("SELECT id FROM users WHERE id = ? FOR UPDATE", user);
        var valid = jdbc.queryForList("""
                SELECT user_id FROM telegram_link_codes WHERE code_hash = ? AND expires_at > now()
                AND EXISTS (SELECT 1 FROM users WHERE id = user_id AND status = 'ACTIVE') FOR UPDATE
                """, UUID.class, hash);
        if (valid.isEmpty()) { throw invalidCode(); }
        jdbc.update("""
                INSERT INTO telegram_link_redemptions (update_id, code_hash, user_id, telegram_user_id, chat_id)
                VALUES (?, ?, ?, ?, ?)
                """, request.updateId(), hash, user, request.telegramUserId(), request.chatId());
        jdbc.update("DELETE FROM telegram_link_codes WHERE code_hash = ?", hash);
        return new Redeemed(user);
    }

    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", "API-ключ недействителен");
    }
    private ApiException invalidCode() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LINK_CODE", "Неверный или просроченный код");
    }
}
