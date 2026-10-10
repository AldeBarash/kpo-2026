package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationPriority;

/**
 * Уведомление по электронной почте с адресом и темой письма.
 * @param recipient получатель уведомления
 * @param text текст сообщения
 * @param createdAt время создания уведомления
 * @param priority приоритет уведомления
 * @param subject тема письма
 */
@Builder
public record EmailNotification(
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority,
        String subject
) implements Notification { }
