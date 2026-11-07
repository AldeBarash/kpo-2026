package studying.notification.factory.impl;

import org.springframework.stereotype.Service;
import studying.notification.factory.NotificationFactory;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.model.PushNotification;
import studying.notification.enums.NotificationChannel;
import studying.notification.exception.UnsupportedNotificationChannelException;

/** Creates push notifications through their Lombok builder. */
@Service
public final class PushNotificationFactory implements NotificationFactory {
    @Override
    public Notification create(final NotificationDraft draft) {
        validateChannel(draft, NotificationChannel.PUSH);
        return PushNotification.builder()
                .recipient(draft.recipient())
                .text(draft.text())
                .createdAt(draft.createdAt())
                .priority(draft.priority())
                .deviceId(draft.recipient())
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
