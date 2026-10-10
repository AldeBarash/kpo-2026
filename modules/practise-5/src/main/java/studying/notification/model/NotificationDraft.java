package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.exception.NotificationValidationException;

/** Immutable validated data from which a notification is created. */
@Builder
public record NotificationDraft(
        NotificationChannel channel,
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority
) {
    /** Validates mandatory notification data. */
    public NotificationDraft {
        if (channel == null || recipient == null || recipient.isBlank()
                || text == null || text.isBlank() || createdAt == null
                || priority == null) {
            throw new NotificationValidationException(
                    "Канал, получатель, текст, время и приоритет обязательны");
        }
    }
}
