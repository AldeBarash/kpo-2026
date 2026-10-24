package studying.delivery.mapper;

import org.springframework.stereotype.Component;
import studying.delivery.dto.DeliveryResponse;
import studying.delivery.model.Delivery;
import studying.notification.enums.NotificationChannel;
import studying.notification.model.EmailNotification;
import studying.notification.model.InAppNotification;
import studying.notification.model.PushNotification;

/** Преобразует внутреннее состояние в публичное представление HTTP API. */
@Component
public final class DeliveryResponseMapper {
    /**
     * Строит DTO из неизменяемого снимка.
     * @param delivery доставка
     * @return публичный ответ
     */
    public DeliveryResponse toResponse(Delivery delivery) {
        var notification = delivery.notification();
        var channel = switch (notification) {
            case EmailNotification email -> NotificationChannel.EMAIL;
            case PushNotification push -> NotificationChannel.PUSH;
            case InAppNotification inApp -> NotificationChannel.IN_APP;
            default -> throw new IllegalStateException(
                    "Неизвестный тип уведомления");
        };
        return new DeliveryResponse(delivery.id(), channel,
                notification.recipient(), notification.text(),
                notification.priority(), delivery.status(),
                delivery.attemptsMade(), delivery.nextAttemptAt());
    }
}
