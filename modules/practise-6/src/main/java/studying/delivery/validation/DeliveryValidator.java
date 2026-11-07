package studying.delivery.validation;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.exception.DeliveryException;
import studying.delivery.model.DeliveryStatus;
import studying.delivery.retry.RetryPolicy;
import studying.exception.ApplicationErrorCode;
import studying.notification.model.Notification;

/** Проверяет обязательные поля и согласованность состояния доставки. */
@Component
@RequiredArgsConstructor
public final class DeliveryValidator {
    private final DeliveryValidation validation;

    /**
     * Проверяет данные перед созданием объекта.
     * @param id идентификатор доставки
     * @param notification уведомление
     * @param policy политика повторов
     * @param attemptsMade число выполненных попыток
     * @param status статус доставки
     * @param nextAttemptAt время следующей попытки
     */
    public void validate(UUID id, Notification notification,
                                RetryPolicy policy,
                                int attemptsMade, DeliveryStatus status,
                                Instant nextAttemptAt) {
        validation.required(id,
                "Идентификатор доставки обязателен");
        validation.required(notification,
                "Уведомление обязательно");
        validation.required(policy,
                "Политика повторов обязательна");
        validation.required(status,
                "Статус доставки обязателен");
        if (attemptsMade < 0
                || (status == DeliveryStatus.PENDING)
                != (nextAttemptAt != null)
                || (status != DeliveryStatus.PENDING && attemptsMade == 0)) {
            throw new DeliveryException(
                    ApplicationErrorCode.INVALID_DELIVERY_STATE,
                    "Некорректное состояние доставки");
        }
    }
}
