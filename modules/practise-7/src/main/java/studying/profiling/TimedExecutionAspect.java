package studying.profiling;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/** Логирует результат метода и записывает время выполнения в Micrometer. */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public final class TimedExecutionAspect {
    private final MeterRegistry registry;

    /**
     * Выбирает методы с аннотацией профилирования.
     * @param settings параметры аннотации метода
     */
    @Pointcut(value = "@annotation(settings)", argNames = "settings")
    private void timedMethod(TimedExecution settings) { }

    /**
     * Сохраняет результат или исключение в лог, а длительность — в таймер.
     * @param call перехваченный вызов
     * @param settings параметры аннотации метода
     * @return исходный результат метода
     * @throws Throwable исходное исключение метода
     */
    @Around(value = "timedMethod(settings)", argNames = "call,settings")
    public Object profile(ProceedingJoinPoint call, TimedExecution settings)
            throws Throwable {
        log.info("operation={} started", settings.operation());
        Timer timer = Timer.builder(settings.operation())
                .publishPercentileHistogram()
                .register(registry);
        Timer.Sample sample = Timer.start(registry);
        try {
            Object result = call.proceed();
            if (settings.logDetails()) {
                log.info("operation={} result={}", settings.operation(), result);
            }
            return result;
        } catch (Throwable exception) {
            if (settings.logDetails()) {
                log.error("operation={} failed", settings.operation(), exception);
            }
            throw exception;
        } finally {
            sample.stop(timer);
        }
    }
}
