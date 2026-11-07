package studying.delivery.observer;

import studying.delivery.event.DeliveryAttemptFinished;

/**
 * Диагностика сбоя наблюдателя с событием и исходным исключением.
 * @param observer наблюдатель, обработка которого завершилась ошибкой
 * @param event событие завершённой попытки
 * @param cause исходное исключение наблюдателя
 */
public record ObserverFailure(DeliveryObserver observer,
                              DeliveryAttemptFinished event,
                              RuntimeException cause) { }
