package studying.delivery.sender;

import studying.delivery.model.AttemptResult;
import studying.notification.model.Notification;

/** Выполняет одну отправку и возвращает ожидаемые ошибки как результат. */
@FunctionalInterface
public interface NotificationSender {
    /**
     * Отправляет уведомление без повторных попыток.
     * @param notification неизменяемое уведомление
     * @return результат выполненной попытки
     */
    AttemptResult send(Notification notification);
}
