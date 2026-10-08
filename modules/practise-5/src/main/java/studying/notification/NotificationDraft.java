package studying.notification;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder(builderClassName = "NotificationDraftBuilder")
public class NotificationDraft {
    NotificationChannel channel;
    String recipient;
    String text;
    LocalDateTime createdAt;
    Priority priority;

    // Кастомный строитель Lombok для перехвата валидации перед сборкой
    public static class NotificationDraftBuilder {
        public NotificationDraft build() {
            if (this.channel == null) {
                throw new IllegalArgumentException("Канал доставки обязателен");
            }
            if (this.recipient == null || this.recipient.trim().isEmpty()) {
                throw new IllegalArgumentException("Получатель обязателен");
            }
            if (this.text == null || this.text.trim().isEmpty()) {
                throw new IllegalArgumentException("Текст уведомления не может быть пустым");
            }
            if (this.createdAt == null) {
                throw new IllegalArgumentException("Время создания должно быть задано");
            }
            if (this.priority == null) {
                throw new IllegalArgumentException("Приоритет должен быть задан");
            }
            return new NotificationDraft(this.channel, this.recipient, this.text, this.createdAt, this.priority);
        }
    }
}
