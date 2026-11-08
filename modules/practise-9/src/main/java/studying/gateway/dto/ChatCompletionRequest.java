package studying.gateway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChatCompletionRequest(
        @NotBlank String model,
        @NotBlank String prompt,
        @Min(1) @Max(2048) int maxTokens
) {
}
