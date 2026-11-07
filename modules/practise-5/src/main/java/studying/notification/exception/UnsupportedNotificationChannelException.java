package studying.notification.exception;

/** Raised when a factory cannot create a notification for a channel. */
public final class UnsupportedNotificationChannelException
        extends IllegalArgumentException {
    /**
     * Creates an exception with an unsupported-channel description.
     *
     * @param message unsupported-channel description
     */
    public UnsupportedNotificationChannelException(final String message) {
        super(message);
    }
}
