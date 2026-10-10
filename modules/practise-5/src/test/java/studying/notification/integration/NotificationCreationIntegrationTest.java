package studying.notification.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import studying.Main;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.factory.NotificationFactory;
import studying.notification.services.NotificationFactoryResolver;
import studying.notification.exception.UnsupportedNotificationChannelException;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.factory.impl.InAppNotificationFactory;
import studying.notification.factory.impl.PushNotificationFactory;
import studying.notification.model.EmailNotification;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;

/** Integration test for building a draft and creating a notification. */
@DisplayName("Интеграционные тесты создания уведомлений")
@SpringBootTest(classes = Main.class)
class NotificationCreationIntegrationTest {
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 10, 1, 10, 15);

    @Autowired
    private NotificationFactoryResolver resolver;

    @Autowired
    private ApplicationContext context;

    @ParameterizedTest
    @EnumSource(NotificationChannel.class)
    @DisplayName("Резолвер возвращает бин фабрики для каждого канала")
    void resolvesSpringFactoryBean(final NotificationChannel channel) {
        final Class<? extends NotificationFactory> factoryType =
                switch (channel) {
                    case EMAIL -> EmailNotificationFactory.class;
                    case PUSH -> PushNotificationFactory.class;
                    case IN_APP -> InAppNotificationFactory.class;
                };
        assertSame(context.getBean(factoryType), resolver.forChannel(channel));
    }

    @Test
    @DisplayName("Резолвер отклоняет отсутствующий канал")
    void rejectsMissingChannel() {
        assertThrows(UnsupportedNotificationChannelException.class,
                () -> resolver.forChannel(null));
    }

    @Test
    @DisplayName("Черновик и абстрактная фабрика создают email-уведомление")
    void createsEmailNotificationFromDraft() {
        final NotificationDraft draft = NotificationDraft.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("student@example.org")
                .text("Ваш заказ передан в доставку")
                .createdAt(CREATED_AT)
                .priority(NotificationPriority.HIGH)
                .build();

        final NotificationFactory factory = resolver.forChannel(
                draft.channel());
        final Notification notification = factory.create(draft);

        final EmailNotification email = assertInstanceOf(
                EmailNotification.class, notification);
        assertEquals(draft.recipient(), email.recipient());
        assertEquals(draft.text(), email.text());
        assertEquals(draft.createdAt(), email.createdAt());
        assertEquals(draft.priority(), email.priority());
    }
}
