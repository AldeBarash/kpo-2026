package studying.notification.validation;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import studying.notification.enums.NotificationChannel;
import studying.notification.exception.UnsupportedNotificationChannelException;
import studying.notification.model.NotificationDraft;

/** Проверки наличия и поддержки канала уведомления. */
@Component
@NoArgsConstructor
public final class NotificationChannelValidator {
    /**
     * Проверяет наличие канала.
     * @param channel запрошенный канал
     */
    public void validatePresent(NotificationChannel channel) {
        if (channel == null) {
            throw new UnsupportedNotificationChannelException(
                    "Канал уведомления не задан");
        }
    }

    /**
     * Проверяет соответствие канала черновика выбранной фабрике.
     * @param draft черновик уведомления
     * @param expected поддерживаемый канал
     */
    public void validateSupported(NotificationDraft draft,
                                         NotificationChannel expected) {
        if (draft.channel() != expected) {
            throw new UnsupportedNotificationChannelException(
                    "Фабрика не поддерживает канал " + draft.channel());
        }
    }
}
