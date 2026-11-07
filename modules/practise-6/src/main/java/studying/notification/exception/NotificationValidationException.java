package studying.notification.exception;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

/** Ошибка данных уведомления, нарушающих правила валидации. */
public final class NotificationValidationException
        extends ApplicationException {
    /**
     * Создаёт исключение с описанием ошибки валидации.
     *
     * @param message описание ошибки валидации
     */
    public NotificationValidationException(String message) {
        super(ApplicationErrorCode.NOTIFICATION_VALIDATION_ERROR,
                message);
    }
}
