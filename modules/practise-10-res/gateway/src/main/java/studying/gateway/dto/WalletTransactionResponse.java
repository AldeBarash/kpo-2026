package studying.gateway.dto;

import java.time.Instant;
import java.util.UUID;
import studying.gateway.domain.WalletTransactionType;

public record WalletTransactionResponse(
        UUID id,
        WalletTransactionType type,
        long amount,
        long balanceAfter,
        long reservedAfter,
        Instant createdAt
) {
}
