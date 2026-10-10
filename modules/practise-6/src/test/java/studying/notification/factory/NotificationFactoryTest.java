package studying.notification.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.factory.impl.InAppNotificationFactory;
import studying.notification.factory.impl.PushNotificationFactory;
import studying.notification.model.EmailNotification;
import studying.notification.model.InAppNotification;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.model.PushNotification;
import studying.notification.validation.NotificationChannelValidator;

/** Модульные тесты конкретных фабрик уведомлений. */
@DisplayName("Тесты фабрик уведомлений")
class NotificationFactoryTest {
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 10, 1, 10, 15);
    private static final String TEXT = "Ваш заказ передан в доставку";

    @Test
    @DisplayName("Email-фабрика строит письмо через Builder")
    void emailFactoryBuildsEmailNotification() {
        Notification notification = new EmailNotificationFactory(
                new NotificationChannelValidator()).create(
                draft(NotificationChannel.EMAIL, "student@example.org"));

        EmailNotification email = assertInstanceOf(
                EmailNotification.class, notification);
        assertCommonFields(email, "student@example.org");
        assertEquals("Уведомление магазина", email.subject());
    }

    @Test
    @DisplayName("Push-фабрика строит push-уведомление через Builder")
    void pushFactoryBuildsPushNotification() {
        Notification notification = new PushNotificationFactory(
                new NotificationChannelValidator()).create(
                draft(NotificationChannel.PUSH, "device-42"));

        PushNotification push = assertInstanceOf(PushNotification.class,
                notification);
        assertCommonFields(push, "device-42");
        assertEquals("device-42", push.deviceId());
    }

    @Test
    @DisplayName("In-app фабрика строит уведомление через Builder")
    void inAppFactoryBuildsInAppNotification() {
        Notification notification = new InAppNotificationFactory(
                new NotificationChannelValidator()).create(
                draft(NotificationChannel.IN_APP, "user-42"));

        InAppNotification inApp = assertInstanceOf(
                InAppNotification.class, notification);
        assertCommonFields(inApp, "user-42");
        assertEquals("user-42", inApp.userId());
    }

    private NotificationDraft draft(NotificationChannel channel,
                                    String recipient) {
        return NotificationDraft.builder()
                .channel(channel)
                .recipient(recipient)
                .text(TEXT)
                .createdAt(CREATED_AT)
                .priority(NotificationPriority.NORMAL)
                .build();
    }

    private void assertCommonFields(Notification notification,
                                    String recipient) {
        assertEquals(recipient, notification.recipient());
        assertEquals(TEXT, notification.text());
        assertEquals(CREATED_AT, notification.createdAt());
        assertEquals(NotificationPriority.NORMAL, notification.priority());
    }
}
