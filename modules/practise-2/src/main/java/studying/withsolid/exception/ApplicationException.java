package studying.withsolid.exception;

import lombok.Getter;

/**
 * Прикладное исключение, содержащее код ошибки и сохраняющее исходную причину.
 */
@Getter
public class ApplicationException extends RuntimeException {
    private final ApplicationErrorCode errorCode;

    /**
     * Конструктор для ошибок валидации.
     */
    public ApplicationException(ApplicationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Конструктор для ошибок с сохранением исходной причины сбоя.
     */
    public ApplicationException(ApplicationErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ApplicationErrorCode getCode() {
        return this.errorCode;
    }
}
