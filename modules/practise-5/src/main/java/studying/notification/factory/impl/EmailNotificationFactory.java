package studying.notification.factory.impl;

import org.springframework.stereotype.Service;
import studying.notification.factory.NotificationFactory;
import studying.notification.model.EmailNotification;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.enums.NotificationChannel;
import studying.notification.exception.UnsupportedNotificationChannelException;

/** Creates email notifications through their Lombok builder. */
@Service
public final class EmailNotificationFactory implements NotificationFactory {
    /** Email subject used for store notifications. */
    private static final String SUBJECT = "Уведомление магазина";

    @Override
    public Notification create(final NotificationDraft draft) {
        validateChannel(draft, NotificationChannel.EMAIL);
        return EmailNotification.builder()
                .recipient(draft.recipient())
                .text(draft.text())
                .createdAt(draft.createdAt())
                .priority(draft.priority())
                .subject(SUBJECT)
                .build();
    }

    private void validateChannel(final NotificationDraft draft,
                                 final NotificationChannel expected) {
        if (draft.channel() != expected) {
            throw new UnsupportedNotificationChannelException(
                    "Фабрика не поддерживает канал " + draft.channel());
        }
    }
}
