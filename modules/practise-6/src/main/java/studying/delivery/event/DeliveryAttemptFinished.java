package studying.delivery.event;

import java.time.Instant;
import java.util.UUID;
import studying.delivery.model.AttemptResult;
import studying.delivery.model.DeliveryStatus;

/**
 * Неизменяемое событие с результатом попытки и состоянием доставки.
 * @param deliveryId постоянный идентификатор доставки
 * @param attemptNumber номер попытки, начиная с единицы
 * @param attemptedAt явно переданное время обработки
 * @param result результат попытки с причиной ошибки при наличии
 * @param status состояние доставки после попытки
 * @param nextAttemptAt время следующей попытки или null после завершения
 */
public record DeliveryAttemptFinished(UUID deliveryId,
                                      int attemptNumber,
                                      Instant attemptedAt,
                                      AttemptResult result,
                                      DeliveryStatus status,
                                      Instant nextAttemptAt) { }
