package studying.notification;

import org.springframework.stereotype.Service;

@Service("EMAIL") // Добавили @Service для Spring Boot
public class EmailNotificationFactory implements NotificationFactory {
    @Override
    public Notification create(NotificationDraft draft) {
        return EmailNotification.builder()
                .emailAddress(draft.getRecipient())
                .text(draft.getText())
                .createdAt(draft.getCreatedAt())
                .priority(draft.getPriority())
                .subject("Уведомление магазина")
                .build();
    }
}
