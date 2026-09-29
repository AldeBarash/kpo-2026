package studying.exception;

/** Runtime exception with an application-level error code and root cause. */
public final class ApplicationException extends RuntimeException {
    /** Application-specific error code. */
    private final ApplicationErrorCode code;

    /**
     * Creates an application error without a root cause.
     *
     * @param errorCode machine-readable error code
     * @param message error description
     */
    public ApplicationException(final ApplicationErrorCode errorCode,
                                final String message) {
        super(message);
        this.code = errorCode;
    }

    /**
     * Creates an application error with its original cause.
     *
     * @param errorCode machine-readable error code
     * @param message error description
     * @param cause original exception
     */
    public ApplicationException(final ApplicationErrorCode errorCode,
                                final String message,
                                final Throwable cause) {
        super(message, cause);
        this.code = errorCode;
    }

    /**
     * Returns the machine-readable application error code.
     *
     * @return application error code
     */
    public ApplicationErrorCode getCode() {
        return code;
    }
}
