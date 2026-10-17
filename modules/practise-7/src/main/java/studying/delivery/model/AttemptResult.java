package studying.delivery.model;


/**
 * Результат одной попытки, независимый от итогового статуса доставки.
 * @param kind результат попытки
 * @param reason причина ошибки или null при успехе
 */
public record AttemptResult(Kind kind, String reason) {

    /** Возможные результаты одной попытки отправки. */
    public enum Kind {
        /** Уведомление успешно отправлено. */
        SUCCESS,
        /** Временная ошибка: повторная попытка может помочь. */
        TEMPORARY_FAILURE,
        /** Постоянная ошибка: повторная попытка не поможет. */
        PERMANENT_FAILURE
    }
}
