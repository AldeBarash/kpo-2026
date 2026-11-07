package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/** Push notification addressed to a device. */
@Builder
public record PushNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String deviceId
) implements Notification { }
