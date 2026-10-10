package studying.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import studying.delivery.event.DeliveryAttemptFinished;
import studying.delivery.exception.DeliveryException;
import studying.delivery.mapper.DeliveryMapper;
import studying.delivery.model.AttemptResult;
import studying.delivery.model.Delivery;
import studying.delivery.model.DeliveryStatus;
import studying.delivery.observer.DeliveryJournal;
import studying.delivery.observer.DeliveryObserver;
import studying.delivery.observer.DeliveryStatistics;
import studying.delivery.observer.ObserverFailure;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.retry.LimitedRetryPolicy;
import studying.delivery.retry.NoRetryPolicy;
import studying.delivery.retry.RetryPolicy;
import studying.delivery.sender.InMemoryNotificationSender;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.DeliveryValidator;
import studying.delivery.validation.SenderValidator;
import studying.exception.ApplicationErrorCode;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.model.Notification;
import studying.notification.model.NotificationDraft;
import studying.notification.validation.NotificationChannelValidator;

/** Модульные и интеграционные тесты с явным временем и имитацией отправки. */
@DisplayName("Доставка, журнал и статистика")
class DeliveryServiceTest {
    private static final Instant START = Instant.parse("2026-10-01T10:00:00Z");
    private static final AttemptResult SUCCESS = new AttemptResult(
            AttemptResult.Kind.SUCCESS, null);
    private static final AttemptResult TEMPORARY = new AttemptResult(
            AttemptResult.Kind.TEMPORARY_FAILURE, "Сервис недоступен");
    private static final AttemptResult PERMANENT = new AttemptResult(
            AttemptResult.Kind.PERMANENT_FAILURE, "Адрес некорректен");
    private final DeliveryRepository store = new DeliveryRepository();
    private final DeliveryValidation validation = new DeliveryValidation();
    private final DeliveryMapper deliveryMapper =
            new DeliveryMapper(new DeliveryValidator(validation));
    private final DeliveryJournal journal = new DeliveryJournal();
    private final DeliveryStatistics statistics =
            new DeliveryStatistics(new SimpleMeterRegistry());

    @Test
    @DisplayName("Полный сценарий: ошибка, ожидание, успех и "
            + "отсутствие повторов")
    void completesFullScenario() {
        InMemoryNotificationSender sender = sender(TEMPORARY, SUCCESS);
        List<DeliveryAttemptFinished> extraEvents = new ArrayList<>();
        DeliveryService service = new DeliveryService(sender, store,
                List.of(journal, statistics, extraEvents::add),
                deliveryMapper, validation, new SenderValidator());
        Notification notification = notification();
        Delivery initial =
                service.enqueue(notification, limited(3), START);
        assertEquals(0, initial.attemptsMade());
        assertEquals(START, initial.nextAttemptAt());
        assertTrue(service.processDue(START.minusSeconds(1)).isEmpty());
        assertEquals(0, sender.callCount());
        assertTrue(journal.history(initial.id()).isEmpty());
        assertTrue(service.processDue(START).isEmpty());
        Delivery pending = store.find(initial.id()).orElseThrow();
        assertEquals(1, pending.attemptsMade());
        assertEquals(DeliveryStatus.PENDING, pending.status());
        assertEquals(START.plusSeconds(10), pending.nextAttemptAt());
        assertSame(initial.policy(), pending.policy());
        assertTrue(service.processDue(START.plusSeconds(9)).isEmpty());
        assertEquals(1, sender.callCount());
        assertEquals(1, extraEvents.size());
        assertTrue(service.processDue(START.plusSeconds(10)).isEmpty());
        assertTrue(service.processDue(START.plusSeconds(60)).isEmpty());
        Delivery completed = store.find(initial.id()).orElseThrow();
        assertEquals(DeliveryStatus.SUCCEEDED, completed.status());
        assertNull(completed.nextAttemptAt());
        assertEquals(2, completed.attemptsMade());
        assertEquals(List.of(notification, notification),
                sender.receivedNotifications());
        List<DeliveryAttemptFinished> expected = List.of(
                new DeliveryAttemptFinished(initial.id(), 1, START, TEMPORARY,
                        DeliveryStatus.PENDING, START.plusSeconds(10)),
                new DeliveryAttemptFinished(initial.id(), 2,
                        START.plusSeconds(10), SUCCESS,
                        DeliveryStatus.SUCCEEDED, null));
        assertEquals(expected, journal.history(initial.id()));
        assertEquals(expected, extraEvents);
        assertEquals(1, statistics.successfulAttempts());
        assertEquals(1, statistics.temporaryFailures());
        assertEquals(0, statistics.permanentFailures());
        assertEquals(0, initial.attemptsMade());
        assertThrows(UnsupportedOperationException.class,
                () -> journal.history(initial.id()).clear());
        assertThrows(UnsupportedOperationException.class,
                () -> sender.receivedNotifications().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> store.findAll().clear());
    }

    @ParameterizedTest
    @EnumSource(value = AttemptResult.Kind.class,
            names = {"TEMPORARY_FAILURE", "PERMANENT_FAILURE"})
    @DisplayName("Без повторов первая ошибка завершает доставку навсегда")
    void stopsAfterFirstFailure(AttemptResult.Kind kind) {
        InMemoryNotificationSender sender = sender(
                new AttemptResult(kind, "Ошибка"));
        DeliveryService service = service(sender);
        Delivery delivery = service.enqueue(notification(),
                new NoRetryPolicy(), START);
        service.processDue(START);
        service.processDue(START.plusSeconds(100));
        assertEquals(1, sender.callCount());
        assertEquals(DeliveryStatus.FAILED,
                store.find(delivery.id()).orElseThrow().status());
        assertNull(store.find(delivery.id()).orElseThrow().nextAttemptAt());
        assertEquals(1, journal.history(delivery.id()).size());
    }

    @Test
    @DisplayName("Лимит включает первую попытку, постоянная "
            + "ошибка не повторяется")
    void stopsAtOneOrPermanentFailure() {
        InMemoryNotificationSender sender = sender(TEMPORARY, PERMANENT);
        DeliveryService service = service(sender);
        Delivery first =
                service.enqueue(notification(), limited(1), START);
        Delivery second =
                service.enqueue(notification(), limited(3), START);
        service.processDue(START);
        service.processDue(START.plusSeconds(100));
        for (Delivery delivery : store.findAll()) {
            assertEquals(DeliveryStatus.FAILED, delivery.status());
            assertEquals(1, delivery.attemptsMade());
            assertNull(delivery.nextAttemptAt());
        }
        assertEquals(1, journal.history(first.id()).size());
        assertEquals(1, journal.history(second.id()).size());
        assertEquals(2, sender.callCount());
        assertEquals(1, statistics.temporaryFailures());
        assertEquals(1, statistics.permanentFailures());
    }

    @Test
    @DisplayName("Разные стратегии и расписания независимы в одном сервисе")
    void handlesIndependentDeliveries() {
        InMemoryNotificationSender sender = sender(TEMPORARY, TEMPORARY,
                TEMPORARY, TEMPORARY, SUCCESS);
        DeliveryService service = service(sender);
        Delivery noRetry = service.enqueue(notification(),
                new NoRetryPolicy(), START);
        Delivery limited =
                service.enqueue(notification(), limited(3), START);
        Delivery later = service.enqueue(notification(), limited(3),
                START.plusSeconds(50));
        service.processDue(START);
        assertEquals(2, sender.callCount());
        assertEquals(DeliveryStatus.FAILED,
                store.find(noRetry.id()).orElseThrow().status());
        // Поздний вызов выполняет одну попытку и считает задержку от now.
        service.processDue(START.plusSeconds(20));
        assertEquals(3, sender.callCount());
        assertEquals(START.plusSeconds(40),
                store.find(limited.id()).orElseThrow().nextAttemptAt());
        assertEquals(0, store.find(later.id()).orElseThrow().attemptsMade());
        service.processDue(START.plusSeconds(39));
        assertEquals(3, sender.callCount());
        service.processDue(START.plusSeconds(40));
        assertEquals(DeliveryStatus.FAILED,
                store.find(limited.id()).orElseThrow().status());
        service.processDue(START.plusSeconds(50));
        service.processDue(START.plusSeconds(100));
        assertEquals(5, sender.callCount());
        assertEquals(3, journal.history(limited.id()).size());
        assertEquals(1, journal.history(later.id()).size());
        assertEquals(1, statistics.successfulAttempts());
        assertEquals(4, statistics.temporaryFailures());
    }

    @Test
    @DisplayName("Сбой наблюдателей сохраняется и не мешает "
            + "остальным и отправке")
    void isolatesObserverFailures() {
        InMemoryNotificationSender sender = sender(SUCCESS, TEMPORARY,
                SUCCESS);
        RuntimeException cause =
                new IllegalStateException("Сбой журнала");
        DeliveryObserver broken = event -> {
            throw cause;
        };
        DeliveryObserver alsoBroken = event -> {
            throw cause;
        };
        DeliveryService service = new DeliveryService(sender, store,
                List.of(broken, journal, alsoBroken, statistics),
                deliveryMapper, validation, new SenderValidator());
        Delivery success =
                service.enqueue(notification(), limited(3), START);
        Delivery retry =
                service.enqueue(notification(), limited(3), START);
        List<ObserverFailure> errors = service.processDue(START);
        assertEquals(4, errors.size());
        assertSame(broken, errors.get(0).observer());
        assertSame(alsoBroken, errors.get(1).observer());
        assertSame(cause, errors.get(0).cause());
        assertEquals(journal.history(success.id()).getFirst(),
                errors.get(0).event());
        assertEquals(DeliveryStatus.SUCCEEDED,
                store.find(success.id()).orElseThrow().status());
        assertEquals(START.plusSeconds(10),
                store.find(retry.id()).orElseThrow().nextAttemptAt());
        assertThrows(UnsupportedOperationException.class, errors::clear);
        assertEquals(2, service.processDue(START.plusSeconds(10)).size());
        assertTrue(service.processDue(START.plusSeconds(100)).isEmpty());
        assertEquals(3, sender.callCount());
        assertEquals(1, journal.history(success.id()).size());
        assertEquals(2, journal.history(retry.id()).size());
        assertEquals(2, statistics.successfulAttempts());
        assertEquals(1, statistics.temporaryFailures());
    }

    @Test
    @DisplayName("После успеха политика не вызывается; "
            + "отправитель не выдумывает успех")
    void skipsPolicyOnSuccess() {
        InMemoryNotificationSender sender = sender(SUCCESS);
        DeliveryService service = service(sender);
        RetryPolicy policy = (attempts, failure) -> {
            throw new AssertionError("После успеха политика не нужна");
        };
        service.enqueue(notification(), policy, START);
        service.processDue(START);
        service.processDue(START.plusSeconds(100));
        assertEquals(1, sender.callCount());
        DeliveryException error = assertThrows(DeliveryException.class,
                () -> sender.send(notification()));
        assertEquals(ApplicationErrorCode.SENDER_RESULTS_EXHAUSTED,
                error.getCode());
    }

    private DeliveryService service(InMemoryNotificationSender sender) {
        return new DeliveryService(sender, store, List.of(journal, statistics),
                deliveryMapper, validation, new SenderValidator());
    }

    private static InMemoryNotificationSender sender(
            AttemptResult... outcomes) {
        return new InMemoryNotificationSender(List.of(outcomes),
                new DeliveryValidation(), new SenderValidator());
    }

    private static RetryPolicy limited(int attempts) {
        return new LimitedRetryPolicy(attempts, Duration.ofSeconds(10));
    }

    private static Notification notification() {
        NotificationDraft draft = NotificationDraft.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("student@example.org")
                .text("Заказ передан в доставку")
                .createdAt(LocalDateTime.of(2026, 9, 30, 12, 0))
                .priority(NotificationPriority.HIGH)
                .build();
        return new EmailNotificationFactory(
                new NotificationChannelValidator()).create(draft);
    }
}
