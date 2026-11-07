package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/** Email notification with an address and a mail subject. */
@Builder
public record EmailNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String subject
) implements Notification { }
