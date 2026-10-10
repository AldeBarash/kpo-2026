package studying.notification.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studying.notification.enums.NotificationChannel;
import studying.notification.factory.NotificationFactory;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.model.PushNotification;
import studying.notification.validation.NotificationChannelValidator;

/** Создаёт push-уведомления через Lombok Builder. */
@Service
@RequiredArgsConstructor
public final class PushNotificationFactory implements NotificationFactory {
    private final NotificationChannelValidator channelValidator;

    @Override
    public Notification create(NotificationDraft draft) {
        channelValidator.validateSupported(draft,
                NotificationChannel.PUSH);
        return PushNotification.builder()
                .recipient(draft.recipient())
                .text(draft.text())
                .createdAt(draft.createdAt())
                .priority(draft.priority())
                .deviceId(draft.recipient())
                .build();
    }


}
