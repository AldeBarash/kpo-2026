package studying.gateway.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email, long balance, long availableBalance) {
}
