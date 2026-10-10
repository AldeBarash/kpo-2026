package studying.notification.model;

import java.time.LocalDateTime;
import lombok.Builder;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;

/**
 * Неизменяемый проверенный черновик для создания уведомления.
 * @param channel канал доставки
 * @param recipient получатель уведомления
 * @param text текст сообщения
 * @param createdAt время создания уведомления
 * @param priority приоритет уведомления
 */
@Builder
public record NotificationDraft(
        NotificationChannel channel,
        String recipient,
        String text,
        LocalDateTime createdAt,
        NotificationPriority priority
) { }
