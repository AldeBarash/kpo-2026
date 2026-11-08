package studying.gateway.dto;

import java.util.UUID;

public record ChatCompletionResponse(
        UUID requestId,
        String model,
        String content,
        UsageResponse usage,
        long balance
) {
    public record UsageResponse(
            int promptTokens,
            int completionTokens,
            int totalTokens,
            long chargedCredits
    ) {
    }
}
