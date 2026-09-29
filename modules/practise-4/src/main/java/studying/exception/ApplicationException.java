package studying.exception;

import lombok.Getter;

/** Runtime exception with an application-level error code and root cause. */
@Getter
public class ApplicationException extends RuntimeException {
    /** Application-specific error code. */
    private final ApplicationErrorCode code;

    /**
     * Creates an application error.
     *
     * @param errorCode error code
     * @param message description
     * @param cause original exception, or {@code null} when there is no root
     *     cause
     */
    public ApplicationException(final ApplicationErrorCode errorCode,
                                final String message,
                                final Throwable cause) {
        super(message, cause);
        this.code = errorCode;
    }

    /**
     * Creates an application error.
     *
     * @param errorCode error code
     * @param message description
     */
    public ApplicationException(final ApplicationErrorCode errorCode,
                                final String message) {
        super(message);
        this.code = errorCode;
    }
}
