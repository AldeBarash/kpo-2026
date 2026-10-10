package studying.delivery.mapper;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.model.Delivery;
import studying.delivery.model.DeliveryStatus;
import studying.delivery.retry.RetryPolicy;
import studying.delivery.validation.DeliveryValidator;
import studying.notification.model.Notification;

/** Создаёт проверенные снимки состояния доставки. */
@Component
@RequiredArgsConstructor
public final class DeliveryMapper {
    private final DeliveryValidator validator;

    /**
     * Проверяет данные и создаёт снимок доставки.
     * @return проверенный снимок доставки
     */
    public Delivery create(UUID id, Notification notification, RetryPolicy policy,
                           int attemptsMade, DeliveryStatus status,
                           Instant nextAttemptAt) {
        validator.validate(id, notification, policy, attemptsMade, status,
                nextAttemptAt);
        return new Delivery(id, notification, policy, attemptsMade, status,
                nextAttemptAt);
    }
}
