package studying.delivery.observer;

import studying.delivery.event.DeliveryAttemptFinished;

/** Независимый синхронный получатель результатов попыток. */
@FunctionalInterface
public interface DeliveryObserver {
    /**
     * Обрабатывает событие, не управляя доставкой и повторами.
     * @param event неизменяемое событие завершённой попытки
     */
    void onAttemptFinished(DeliveryAttemptFinished event);
}
