package studying.delivery.observer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import studying.delivery.event.DeliveryAttemptFinished;
import studying.delivery.model.AttemptResult;

/** Сохраняет статистику попыток в Micrometer для публикации через Actuator. */
@Slf4j
@Component
@RequiredArgsConstructor
public final class DeliveryStatistics implements DeliveryObserver {
    private final MeterRegistry registry;
    private final Map<AttemptResult.Kind, Counter> counts =
            new EnumMap<>(AttemptResult.Kind.class);

    /** Регистрирует счётчики всех результатов после внедрения зависимостей. */
    @PostConstruct
    void registerCounters() {
        for (AttemptResult.Kind kind : AttemptResult.Kind.values()) {
            counts.put(kind, Counter.builder("delivery.attempts")
                    .description("Число попыток доставки по результатам")
                    .tag("result", kind.name().toLowerCase(Locale.ROOT))
                    .register(registry));
        }
    }

    @Override
    public void onAttemptFinished(DeliveryAttemptFinished event) {
        log.info("started");
        registerCounters();
        counts.get(event.result().kind()).increment();
        log.info("end");
    }

    /** @return число успешных попыток */
    public int successfulAttempts() {
        registerCounters();
        return (int) counts.get(AttemptResult.Kind.SUCCESS).count();
    }

    /** @return число попыток с временной ошибкой */
    public int temporaryFailures() {
        registerCounters();
        return (int) counts.get(AttemptResult.Kind.TEMPORARY_FAILURE).count();
    }

    /** @return число попыток с постоянной ошибкой */
    public int permanentFailures() {
        registerCounters();
        return (int) counts.get(AttemptResult.Kind.PERMANENT_FAILURE).count();
    }
}
