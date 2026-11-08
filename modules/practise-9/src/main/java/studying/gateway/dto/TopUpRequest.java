package studying.gateway.dto;

import jakarta.validation.constraints.Min;

public record TopUpRequest(
        @Min(1) long amount
) {
}
