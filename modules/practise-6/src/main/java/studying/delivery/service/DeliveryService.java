package studying.delivery.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import studying.delivery.event.DeliveryAttemptFinished;
import studying.delivery.exception.DeliveryException;
import studying.delivery.mapper.DeliveryMapper;
import studying.delivery.model.AttemptResult;
import studying.delivery.model.Delivery;
import studying.delivery.model.DeliveryStatus;
import studying.delivery.observer.DeliveryObserver;
import studying.delivery.observer.ObserverFailure;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.retry.RetryDecision;
import studying.delivery.retry.RetryPolicy;
import studying.delivery.sender.NotificationSender;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.SenderValidator;
import studying.exception.ApplicationErrorCode;
import studying.notification.model.Notification;

/** Управляет отправкой, повторами и оповещением наблюдателей. */
@RequiredArgsConstructor
public final class DeliveryService {
    private final NotificationSender sender;
    private final DeliveryRepository store;
    private final List<DeliveryObserver> observers;
    private final DeliveryMapper deliveryMapper;
    private final DeliveryValidation validation;
    private final SenderValidator senderValidator;

    /**
     * Добавляет доставку в очередь без немедленной отправки.
     * @param notification неизменяемое уведомление
     * @param policy политика повторов этой доставки
     * @param readyAt время готовности первой попытки
     * @return начальный снимок состояния доставки
     */
    public Delivery enqueue(Notification notification,
                            RetryPolicy policy, Instant readyAt) {
        validation.required(readyAt,
                "Время готовности обязательно");
        Delivery delivery = deliveryMapper.create(UUID.randomUUID(), notification,
                policy, 0, DeliveryStatus.PENDING, readyAt);
        store.save(delivery);
        return delivery;
    }

    /**
     * Обрабатывает каждую готовую доставку не более одного раза за вызов.
     * @param now время обработки, включая границу готовности
     * @return неизменяемый список сбоев наблюдателей за вызов
     */
    public List<ObserverFailure> processDue(Instant now) {
        validation.required(now,
                "Время обработки обязательно");
        List<ObserverFailure> errors = new ArrayList<>();
        for (Delivery delivery : store.findAll()) {
            if (delivery.status() != DeliveryStatus.PENDING
                    || delivery.nextAttemptAt().isAfter(now)) {
                continue;
            }
            AttemptResult result = sender.send(delivery.notification());
            senderValidator.validateResult(result);
            Delivery updated = finishAttempt(delivery, result, now);
            store.save(updated);
            DeliveryAttemptFinished event = new DeliveryAttemptFinished(
                    updated.id(), updated.attemptsMade(), now, result,
                    updated.status(), updated.nextAttemptAt());
            for (DeliveryObserver observer : observers) {
                try {
                    observer.onAttemptFinished(event);
                } catch (RuntimeException exception) {
                    errors.add(new ObserverFailure(observer, event, exception));
                }
            }
        }
        return List.copyOf(errors);
    }

    private Delivery finishAttempt(Delivery delivery,
                                   AttemptResult result,
                                   Instant now) {
        int attempts = delivery.attemptsMade() + 1;
        DeliveryStatus status = DeliveryStatus.SUCCEEDED;
        Instant nextAttemptAt = null;
        if (result.kind() != AttemptResult.Kind.SUCCESS) {
            RetryDecision decision = delivery.policy().decide(
                    attempts, result);
            switch (decision) {
                case RetryDecision.Stop stop -> status = DeliveryStatus.FAILED;
                case RetryDecision.RetryAfter retry -> {
                    status = DeliveryStatus.PENDING;
                    nextAttemptAt = now.plus(retry.delay());
                }
                case null, default -> throw new DeliveryException(
                        ApplicationErrorCode.INVALID_RETRY_DECISION,
                        "Политика вернула неподдерживаемое решение");
            }
        }
        return deliveryMapper.create(delivery.id(), delivery.notification(),
                delivery.policy(), attempts, status, nextAttemptAt);
    }
}
