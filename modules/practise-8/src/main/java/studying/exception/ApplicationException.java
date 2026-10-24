package studying.exception;

/** Базовое исключение приложения с машинно-читаемым кодом ошибки. */
public class ApplicationException extends RuntimeException {
    private final ApplicationErrorCode code;

    /**
     * Создаёт ошибку приложения.
     * @param errorCode код ошибки
     * @param message описание ошибки
     */
    public ApplicationException(ApplicationErrorCode errorCode,
                                String message) {
        super(message);
        code = errorCode;
    }

    /** @return код ошибки приложения */
    public final ApplicationErrorCode getCode() {
        return code;
    }
}
