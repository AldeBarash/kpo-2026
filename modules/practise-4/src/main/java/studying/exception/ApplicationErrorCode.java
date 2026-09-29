package studying.exception;

/** Codes that identify errors raised by the report application. */
public enum ApplicationErrorCode {
    /** Input validation failed. */
    VALIDATION_ERROR,
    /** A report could not be written to storage. */
    FILE_WRITE_ERROR
}
