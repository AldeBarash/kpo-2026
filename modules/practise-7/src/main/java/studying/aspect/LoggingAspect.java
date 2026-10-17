package studying.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import studying.annotation.TimedExecution;
import studying.metrics.ExecutionMetrics;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j // Автоматический логгер от Lombok (подключен в build.gradle.kts)
public class LoggingAspect {

    private final ExecutionMetrics metrics;

    public LoggingAspect(ExecutionMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@within(timedExecution) || @annotation(timedExecution)")
    public Object profile(ProceedingJoinPoint joinPoint, TimedExecution timedExecution) throws Throwable {
        String opName = timedExecution.operation();
        Instant start = Instant.now();
        boolean success = false;

        try {
            Object result = joinPoint.proceed();
            success = true; // Вызов прошел без исключений

            if (timedExecution.logDetails()) {
                long durationMs = Duration.between(start, Instant.now()).toMillis();
                // Этот лог благодаря файлу logback-spring.xml уйдет в Kibana в формате JSON!
                log.info("Operation: {}, Status: SUCCESS, Duration: {}ms, Result: {}", opName, durationMs, result);
            }
            return result;
        } catch (Throwable e) {
            if (timedExecution.logDetails()) {
                long durationMs = Duration.between(start, Instant.now()).toMillis();
                log.error("Operation: {}, Status: FAILED, Duration: {}ms, Error: {}", opName, durationMs, e.getMessage());
            }
            throw e; // Пробрасываем исключение дальше, как требует задание
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            metrics.recordExecution(opName, duration, success);
        }
    }
}
