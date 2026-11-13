package studying.application.exception;

public class ApplicationException extends RuntimeException {
    private final ApplicationErrorCode code;
    /**
     * Создаёт ошибку приложения.
     *
     * @param errorCode код ошибки
     * @param message описание ошибки
     */
    public ApplicationException(final ApplicationErrorCode errorCode,
                                final String message) {
        super(message);
        this.code = errorCode;
    }
    /** @return код ошибки */
    public final ApplicationErrorCode getCode() {
        return code;
    }
}
