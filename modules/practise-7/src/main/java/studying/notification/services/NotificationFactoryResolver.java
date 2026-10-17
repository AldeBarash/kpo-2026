package studying.notification.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studying.notification.enums.NotificationChannel;
import studying.notification.factory.NotificationFactory;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.factory.impl.InAppNotificationFactory;
import studying.notification.factory.impl.PushNotificationFactory;
import studying.notification.validation.NotificationChannelValidator;

/** Выбирает фабрику по каналу уведомления. */
@Service
@RequiredArgsConstructor
public final class NotificationFactoryResolver {
    private final NotificationChannelValidator channelValidator;
    private final EmailNotificationFactory emailFactory;
    private final PushNotificationFactory pushFactory;
    private final InAppNotificationFactory inAppFactory;

    /**
     * Возвращает конкретную фабрику для указанного канала.
     *
     * @param channel запрошенный канал уведомления
     * @return фабрика для указанного канала
     */
    public NotificationFactory forChannel(
            NotificationChannel channel) {
        channelValidator.validatePresent(channel);
        return switch (channel) {
            case EMAIL -> emailFactory;
            case PUSH -> pushFactory;
            case IN_APP -> inAppFactory;
        };
    }
}
