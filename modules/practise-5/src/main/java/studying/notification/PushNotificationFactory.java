package studying.notification;

import org.springframework.stereotype.Service;

@Service("PUSH") // Добавили @Service для Spring Boot
public class PushNotificationFactory implements NotificationFactory {
    @Override
    public Notification create(NotificationDraft draft) {
        return PushNotification.builder()
                .deviceId(draft.getRecipient())
                .text(draft.getText())
                .createdAt(draft.getCreatedAt())
                .priority(draft.getPriority())
                .build();
    }
}
