package studying.notification.services;

import org.springframework.stereotype.Service;
import studying.notification.enums.NotificationChannel;
import studying.notification.factory.NotificationFactory;
import studying.notification.exception.UnsupportedNotificationChannelException;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.factory.impl.InAppNotificationFactory;
import studying.notification.factory.impl.PushNotificationFactory;

/** Selects an abstract factory for a requested notification channel. */
@Service
public final class NotificationFactoryResolver {
    private final EmailNotificationFactory emailFactory;
    private final PushNotificationFactory pushFactory;
    private final InAppNotificationFactory inAppFactory;

    /**
     * Creates a resolver with factories supplied by Spring.
     *
     * @param email email notification factory
     * @param push push notification factory
     * @param inApp in-application notification factory
     */
    public NotificationFactoryResolver(
            final EmailNotificationFactory email,
            final PushNotificationFactory push,
            final InAppNotificationFactory inApp) {
        this.emailFactory = email;
        this.pushFactory = push;
        this.inAppFactory = inApp;
    }

    /**
     * Selects a concrete factory for a channel.
     *
     * @param channel requested notification channel
     * @return factory for the channel
     */
    public NotificationFactory forChannel(
            final NotificationChannel channel) {
        if (channel == null) {
            throw new UnsupportedNotificationChannelException(
                    "Канал уведомления не задан");
        }
        return switch (channel) {
            case EMAIL -> emailFactory;
            case PUSH -> pushFactory;
            case IN_APP -> inAppFactory;
        };
    }
}
