package studying.notification.factory;

import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;

/** Abstract factory that turns a draft into a concrete notification. */
public interface NotificationFactory {
    /**
     * Creates a notification for the supplied validated draft.
     *
     * @param draft notification data
     * @return concrete notification
     */
    Notification create(NotificationDraft draft);
}
