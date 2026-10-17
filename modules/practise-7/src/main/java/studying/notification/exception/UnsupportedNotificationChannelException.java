package studying.notification.exception;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

/** Ошибка создания уведомления для неподдерживаемого канала. */
public final class UnsupportedNotificationChannelException
        extends ApplicationException {
    /**
     * Создаёт исключение с описанием неподдерживаемого канала.
     *
     * @param message описание неподдерживаемого канала
     */
    public UnsupportedNotificationChannelException(String message) {
        super(ApplicationErrorCode.UNSUPPORTED_NOTIFICATION_CHANNEL,
                message);
    }
}
