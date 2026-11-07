package studying.config;

import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.delivery.mapper.AttemptResultMapper;
import studying.delivery.mapper.DeliveryMapper;
import studying.delivery.model.AttemptResult;
import studying.delivery.observer.DeliveryJournal;
import studying.delivery.observer.DeliveryObserver;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.sender.InMemoryNotificationSender;
import studying.delivery.sender.NotificationSender;
import studying.delivery.service.DeliveryService;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.SenderValidator;

/** Собирает учебные адаптеры и зависимости бизнес-сценариев. */
@Configuration(proxyBeanMethods = false)
public class DeliveryConfiguration {
    /** @return серверные часы в UTC */
    @Bean
    public Clock deliveryClock() {
        return Clock.systemUTC();
    }

    /** @return общее хранилище доставок */
    @Bean
    public DeliveryRepository deliveryRepository() {
        return new DeliveryRepository();
    }

    /** @return журнал событий попыток */
    @Bean
    public DeliveryJournal deliveryJournal() {
        return new DeliveryJournal();
    }

    /**
     * Создаёт общий сценарий: временная ошибка, затем успех.
     * @param results маппер результатов
     * @param validation проверка обязательных данных
     * @param senderValidator проверка отправителя
     * @return отправитель с двумя результатами
     */
    @Bean
    public NotificationSender notificationSender(AttemptResultMapper results,
            DeliveryValidation validation, SenderValidator senderValidator) {
        return new InMemoryNotificationSender(List.of(
                results.create(AttemptResult.Kind.TEMPORARY_FAILURE,
                        "Учебный шлюз временно недоступен"),
                results.create(AttemptResult.Kind.SUCCESS, null)),
                validation, senderValidator);
    }

    /**
     * Создаёт сервис обработки доставок.
     * @param sender отправитель
     * @param repository хранилище
     * @param observers наблюдатели Spring
     * @param mapper маппер состояния
     * @param validation общие проверки
     * @param senderValidator проверка результата отправителя
     * @return сервис с профилированием через Spring AOP
     */
    @Bean
    public DeliveryService deliveryService(NotificationSender sender,
            DeliveryRepository repository, List<DeliveryObserver> observers,
            DeliveryMapper mapper, DeliveryValidation validation,
            SenderValidator senderValidator) {
        return new DeliveryService(sender, repository, observers, mapper,
                validation, senderValidator);
    }
}
