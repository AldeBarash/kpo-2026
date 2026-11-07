package studying.delivery.dto;

import java.time.Instant;
import java.util.UUID;
import studying.delivery.model.DeliveryStatus;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;

/**
 * Публичное представление доставки без внутренних стратегий.
 * @param id идентификатор
 * @param channel канал
 * @param recipient получатель
 * @param text сообщение
 * @param priority приоритет
 * @param status состояние
 * @param attemptsMade число попыток
 * @param nextAttemptAt следующая попытка или null
 */
public record DeliveryResponse(UUID id, NotificationChannel channel,
        String recipient, String text, NotificationPriority priority,
        DeliveryStatus status, int attemptsMade, Instant nextAttemptAt) { }
