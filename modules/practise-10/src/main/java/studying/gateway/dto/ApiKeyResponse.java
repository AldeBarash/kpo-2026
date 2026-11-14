package studying.gateway.dto;

import java.util.UUID;

public record ApiKeyResponse(UUID id, String key, String prefix) {
}
