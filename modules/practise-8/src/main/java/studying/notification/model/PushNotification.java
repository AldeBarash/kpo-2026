package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/**
 * Push-уведомление, адресованное устройству.
 * @param recipient получатель уведомления
 * @param text текст сообщения
 * @param createdAt время создания уведомления
 * @param priority приоритет уведомления
 * @param deviceId идентификатор устройства для push-уведомлений
 */
@Builder
public record PushNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String deviceId
) implements Notification { }
