package studying.delivery.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import studying.delivery.event.DeliveryAttemptFinished;

@Slf4j
/** Сохраняет историю попыток доставки в памяти. */
public final class DeliveryJournal implements DeliveryObserver {
    private final List<DeliveryAttemptFinished> events = new ArrayList<>();

    @Override
    public void onAttemptFinished(DeliveryAttemptFinished event) {
        log.info("started");
        events.add(event);
        log.info("end");
    }

    /**
     * Возвращает историю попыток в порядке их выполнения.
     * @param deliveryId идентификатор доставки
     * @return неизменяемый снимок истории попыток
     */
    public List<DeliveryAttemptFinished> history(UUID deliveryId) {
        return events.stream()
                .filter(event -> event.deliveryId().equals(deliveryId))
                .toList();
    }
}
