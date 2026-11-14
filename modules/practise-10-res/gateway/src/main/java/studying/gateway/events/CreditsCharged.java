package studying.gateway.events;

import java.time.Instant;
import java.util.UUID;

public record CreditsCharged(UUID eventId, String eventType, int schemaVersion,
        Instant occurredAt, UUID userId, UUID requestId, String model,
        int promptTokens, int completionTokens, long chargedCredits, long balanceAfter) {
    public CreditsCharged {
        if (eventId == null || !"CreditsCharged".equals(eventType) || schemaVersion != 1
                || occurredAt == null || userId == null || requestId == null
                || model == null || model.isBlank() || model.length() > 200
                || promptTokens < 0 || completionTokens < 0
                || chargedCredits < 0 || balanceAfter < 0) {
            throw new IllegalArgumentException("Invalid CreditsCharged v1 event");
        }
    }

    public String notification() {
        return "Запрос " + requestId + " завершён.\nМодель: " + model
                + "\nТокены: " + promptTokens + " входных, " + completionTokens
                + " выходных.\nСписано: " + chargedCredits
                + " кредитов.\nБаланс после операции: " + balanceAfter + " кредитов.";
    }
}
