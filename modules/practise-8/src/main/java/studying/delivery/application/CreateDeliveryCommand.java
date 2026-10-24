package studying.delivery.application;

import java.time.Duration;
import java.time.Instant;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;

/**
 * Входные данные сценария, независимые от HTTP.
 * @param channel канал
 * @param recipient получатель
 * @param text сообщение
 * @param priority приоритет
 * @param readyAt готовность первой попытки
 * @param maxAttempts лимит попыток
 * @param baseDelay базовая задержка
 */
public record CreateDeliveryCommand(NotificationChannel channel,
        String recipient, String text, NotificationPriority priority,
        Instant readyAt, int maxAttempts, Duration baseDelay) { }
