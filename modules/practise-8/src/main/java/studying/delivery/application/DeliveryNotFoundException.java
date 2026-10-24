package studying.delivery.application;

import java.util.UUID;

/** Сценарий не нашёл доставку; HTTP-статус выбирается во внешнем слое. */
public final class DeliveryNotFoundException extends RuntimeException {
    /**
     * Создаёт ошибку поиска.
     * @param id искомая доставка
     */
    public DeliveryNotFoundException(UUID id) {
        super("Доставка не найдена: " + id);
    }
}
