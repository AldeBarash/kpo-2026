package studying.notification.validation;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.exception.NotificationValidationException;

/** Проверяет обязательные данные черновика уведомления. */
@Component
@NoArgsConstructor
public final class NotificationDraftValidator {
    /**
     * Проверяет данные перед созданием объекта.
     * @param channel канал уведомления
     * @param recipient получатель
     * @param text текст сообщения
     * @param createdAt время создания
     * @param priority приоритет
     */
    public void validate(NotificationChannel channel, String recipient,
                                String text, LocalDateTime createdAt,
                                NotificationPriority priority) {
        if (channel == null || recipient == null || recipient.isBlank()
                || text == null || text.isBlank() || createdAt == null
                || priority == null) {
            throw new NotificationValidationException(
                    "Канал, получатель, текст, время и приоритет обязательны");
        }
    }
}
