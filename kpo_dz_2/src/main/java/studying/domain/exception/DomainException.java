package studying.domain.exception;

/** Доменное исключение приложения с машинно-читаемым кодом ошибки. */
public class DomainException extends RuntimeException {
    private final DomainErrorCode code;

    /**
     * Создаёт ошибку приложения.
     * @param errorCode код ошибки
     * @param message описание ошибки
     */
    public DomainException(final DomainErrorCode errorCode,
                                final String message) {
        super(message);
        this.code = errorCode;
    }
    /** @return код ошибки приложения */
    public final DomainErrorCode getCode() {

        return code;
    }
}
