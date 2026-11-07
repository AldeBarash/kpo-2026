package studying.notification.factory.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studying.notification.enums.NotificationChannel;
import studying.notification.factory.NotificationFactory;
import studying.notification.model.EmailNotification;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.validation.NotificationChannelValidator;

/** Создаёт уведомления по электронной почте через Lombok Builder. */
@Service
@RequiredArgsConstructor
public final class EmailNotificationFactory implements NotificationFactory {

    /** Тема письма для уведомлений магазина. */
    private static final String SUBJECT = "Уведомление магазина";

    private final NotificationChannelValidator channelValidator;

    @Override
    public Notification create(NotificationDraft draft) {
        channelValidator.validateSupported(draft,
                NotificationChannel.EMAIL);
        return EmailNotification.builder()
                .recipient(draft.recipient())
                .text(draft.text())
                .createdAt(draft.createdAt())
                .priority(draft.priority())
                .subject(SUBJECT)
                .build();
    }


}
