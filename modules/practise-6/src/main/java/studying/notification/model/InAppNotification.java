package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/**
 * Уведомление для пользователя внутри приложения.
 * @param recipient получатель уведомления
 * @param text текст сообщения
 * @param createdAt время создания уведомления
 * @param priority приоритет уведомления
 * @param userId идентификатор пользователя приложения
 */
@Builder
public record InAppNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String userId
) implements Notification { }
