package studying.delivery.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import studying.delivery.model.Delivery;

/** Хранит ожидающие и завершённые доставки для последовательной обработки. */
public final class DeliveryRepository {
    private final Map<UUID, Delivery> deliveries = new LinkedHashMap<>();

    /**
     * Возвращает последний сохранённый снимок состояния доставки.
     * @param id идентификатор доставки
     * @return доставка, если она найдена
     */
    public Optional<Delivery> find(UUID id) {
        return Optional.ofNullable(deliveries.get(id));
    }

    /** @return неизменяемый список доставок в порядке добавления */
    public List<Delivery> findAll() {
        return List.copyOf(deliveries.values());
    }

    /**
     * Сохраняет новое или обновлённое состояние доставки.
     * @param delivery снимок состояния доставки
     */
    public void save(Delivery delivery) {
        deliveries.put(delivery.id(), delivery);
    }
}
