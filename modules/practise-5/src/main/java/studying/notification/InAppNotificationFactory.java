package studying.notification;

import org.springframework.stereotype.Service;

@Service("IN_APP") // Добавили @Service для Spring Boot
public class InAppNotificationFactory implements NotificationFactory {
    @Override
    public Notification create(NotificationDraft draft) {
        return InAppNotification.builder()
                .userId(draft.getRecipient())
                .text(draft.getText())
                .createdAt(draft.getCreatedAt())
                .priority(draft.getPriority())
                .build();
    }
}
