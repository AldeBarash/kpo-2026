package studying.notification.exception;

/** Raised when notification data does not meet business validation rules. */
public final class NotificationValidationException
        extends IllegalArgumentException {
    /**
     * Creates an exception with a validation error description.
     *
     * @param message validation error description
     */
    public NotificationValidationException(final String message) {
        super(message);
    }
}
