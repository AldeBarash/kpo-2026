package studying.exception;

/** Codes that identify errors raised by the report application. */
public enum ApplicationErrorCode {
    /** Input validation failed. */
    VALIDATION_ERROR,
    /** A report could not be written to storage. */
    FILE_WRITE_ERROR,
    /** A requested service has not been registered. */
    SERVICE_NOT_FOUND
}
