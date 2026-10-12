package studying.notification.mapper;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.model.NotificationDraft;
import studying.notification.validation.NotificationDraftValidator;

/** Создаёт проверенные черновики уведомлений. */
@Component
@RequiredArgsConstructor
public final class NotificationDraftMapper {
    private final NotificationDraftValidator validator;

    /**
     * Проверяет поля и создаёт черновик уведомления.
     * @return проверенный черновик
     */
    public NotificationDraft create(NotificationChannel channel, String recipient,
                                    String text, LocalDateTime createdAt,
                                    NotificationPriority priority) {
        validator.validate(channel, recipient, text, createdAt, priority);
        return new NotificationDraft(channel, recipient, text, createdAt,
                priority);
    }
}
