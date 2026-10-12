package studying.exception;

/** Коды ошибок создания и доставки уведомлений. */
public enum ApplicationErrorCode {
    /** Отсутствует обязательное значение. */
    REQUIRED_VALUE_MISSING,
    /** Состояние доставки не согласовано с расписанием или числом попыток. */
    INVALID_DELIVERY_STATE,
    /** Результат попытки не согласован с причиной ошибки. */
    INVALID_ATTEMPT_RESULT,
    /** Некорректные параметры политики повторов. */
    INVALID_RETRY_POLICY,
    /** Политика вернула неизвестное решение или null. */
    INVALID_RETRY_DECISION,
    /** Задержка повтора должна быть положительной. */
    INVALID_RETRY_DELAY,
    /** Заданная последовательность результатов отправителя исчерпана. */
    SENDER_RESULTS_EXHAUSTED,
    /** Отправитель не вернул результат попытки. */
    SENDER_RESULT_MISSING,
    /** Данные уведомления не прошли проверку. */
    NOTIFICATION_VALIDATION_ERROR,
    /** Канал уведомления не поддерживается. */
    UNSUPPORTED_NOTIFICATION_CHANNEL
}
