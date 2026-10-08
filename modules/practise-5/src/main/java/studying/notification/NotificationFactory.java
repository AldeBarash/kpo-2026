package studying.notification;

public interface NotificationFactory {
    Notification create(NotificationDraft draft);
}
