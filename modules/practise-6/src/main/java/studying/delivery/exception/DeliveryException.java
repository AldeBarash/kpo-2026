package studying.delivery.exception;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

/** Ошибка проверки данных или выполнения доставки уведомления. */
public final class DeliveryException extends ApplicationException {
    /**
     * Создаёт ошибку доставки с указанным кодом.
     * @param errorCode код ошибки
     * @param message описание ошибки
     */
    public DeliveryException(ApplicationErrorCode errorCode,
                             String message) {
        super(errorCode, message);
    }
}
