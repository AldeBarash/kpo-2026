package studying.notification.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studying.notification.enums.NotificationChannel;
import studying.notification.factory.NotificationFactory;
import studying.notification.model.InAppNotification;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.validation.NotificationChannelValidator;

/** Создаёт уведомления внутри приложения через Lombok Builder. */
@Service
@RequiredArgsConstructor
public final class InAppNotificationFactory implements NotificationFactory {
    private final NotificationChannelValidator channelValidator;

    @Override
    public Notification create(NotificationDraft draft) {
        channelValidator.validateSupported(draft,
                NotificationChannel.IN_APP);
        return InAppNotification.builder()
                .recipient(draft.recipient())
                .text(draft.text())
                .createdAt(draft.createdAt())
                .priority(draft.priority())
                .userId(draft.recipient())
                .build();
    }


}
