package exception;

import lombok.Getter;

/** Базовое исключение приложения с кодом ошибки. */
@Getter
public class ApplicationException extends RuntimeException {

    /** Код ошибки. */
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
}
