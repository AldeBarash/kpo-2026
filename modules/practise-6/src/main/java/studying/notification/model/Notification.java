package studying.notification.model;

import java.time.LocalDateTime;
import studying.notification.enums.NotificationPriority;

/** Общий контракт для всех видов уведомлений. */
public interface Notification {
    /** @return получатель уведомления */
    String recipient();

    /** @return текст уведомления */
    String text();

    /** @return время создания уведомления */
    LocalDateTime createdAt();

    /** @return приоритет уведомления */
    NotificationPriority priority();
}
