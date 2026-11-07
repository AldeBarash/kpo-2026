package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/** Notification shown to a user inside the application. */
@Builder
public record InAppNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String userId
) implements Notification { }
