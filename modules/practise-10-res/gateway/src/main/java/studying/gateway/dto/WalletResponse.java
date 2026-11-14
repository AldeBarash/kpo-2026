package studying.gateway.dto;

import java.util.UUID;

public record WalletResponse(
        UUID userId,
        long balance,
        long reservedBalance,
        long availableBalance
) {
}
