package studying.delivery.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studying.delivery.mapper.RetryPolicyMapper;
import studying.delivery.model.Delivery;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.service.DeliveryService;
import studying.notification.mapper.NotificationDraftMapper;
import studying.notification.services.NotificationFactoryResolver;

/**
 * Собирает сценарии и последовательно обращается к общему состоянию.
 * Все HTTP-входы используют этот singleton и один общий монитор.
 */
@Service
@RequiredArgsConstructor
public final class DeliveryApplicationService {
    private final DeliveryService deliveries;
    private final DeliveryRepository repository;
    private final NotificationDraftMapper drafts;
    private final NotificationFactoryResolver factories;
    private final RetryPolicyMapper policies;
    private final Clock clock;

    /**
     * Создаёт уведомление и ставит его в очередь без отправки.
     * @param command данные сценария
     * @return начальное состояние доставки
     */
    public synchronized Delivery create(CreateDeliveryCommand command) {
        var draft = drafts.create(command.channel(), command.recipient(),
                command.text(), LocalDateTime.ofInstant(clock.instant(),
                        ZoneOffset.UTC), command.priority());
        var notification = factories.forChannel(command.channel())
                .create(draft);
        var policy = policies.limited(command.maxAttempts(),
                command.baseDelay());
        return deliveries.enqueue(notification, policy, command.readyAt());
    }

    /**
     * Находит доставку.
     * @param id идентификатор
     * @return сохранённый снимок
     */
    public synchronized Delivery find(UUID id) {
        return repository.find(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));
    }

    /** @return снимки доставок в порядке добавления */
    public synchronized List<Delivery> findAll() {
        return repository.findAll();
    }

    /** @return число ошибок наблюдателей за обработку готовых доставок */
    public synchronized int processDue() {
        return deliveries.processDue(clock.instant()).size();
    }
}
