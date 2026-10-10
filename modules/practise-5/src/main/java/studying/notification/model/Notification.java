package studying.notification.model;

import java.time.LocalDateTime;
import studying.notification.enums.NotificationPriority;

/** Common contract for all notifications. */
public interface Notification {
    /** @return notification recipient */
    String recipient();

    /** @return notification text */
    String text();

    /** @return notification creation time */
    LocalDateTime createdAt();

    /** @return notification priority */
    NotificationPriority priority();
}
