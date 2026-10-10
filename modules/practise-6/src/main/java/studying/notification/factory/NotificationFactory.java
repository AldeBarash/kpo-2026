package studying.notification.factory;

import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;

/** Абстрактная фабрика создания уведомления из черновика. */
public interface NotificationFactory {
    /**
     * Создаёт уведомление из переданного проверенного черновика.
     *
     * @param draft данные уведомления
     * @return уведомление конкретного вида
     */
    Notification create(NotificationDraft draft);
}
