package studying.delivery.model;

import java.time.Instant;
import java.util.UUID;
import studying.delivery.retry.RetryPolicy;
import studying.notification.model.Notification;

/**
 * Неизменяемый снимок доставки с выбранной политикой повторов.
 * @param id постоянный идентификатор доставки
 * @param notification неизменяемое содержимое уведомления
 * @param policy политика, сохраняемая между попытками
 * @param attemptsMade число выполненных попыток
 * @param status состояние доставки после попытки
 * @param nextAttemptAt время следующей попытки или null после завершения
 */
public record Delivery(UUID id, Notification notification, RetryPolicy policy,
                       int attemptsMade, DeliveryStatus status,
                       Instant nextAttemptAt) { }
