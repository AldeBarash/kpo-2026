package studying.profiling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

/** Проверяет профилирование произвольных методов без привязки к доставке. */
class TimedExecutionTest {
    private final MockClock clock = new MockClock();
    private final SimpleMeterRegistry registry =
            new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
    private final ExampleService target = new ExampleService();
    private final ExampleService service = proxy();

    @Test
    void ignoresUnannotatedMethod() {
        assertEquals("plain", service.plain());
        assertTrue(registry.getMeters().isEmpty());
    }

    @Test
    void preservesArbitraryResultAndRecordsTime() {
        Object result = new Object();
        assertSame(result, service.execute(result));
        assertDuration("test.execute");
    }

    @Test
    void handlesNullAndVoidResults() {
        assertNull(service.execute(null));
        service.run();
        assertDuration("test.execute");
        assertDuration("test.run");
    }

    @Test
    void logsResultAndOriginalException() {
        Logger logger = (Logger) LoggerFactory.getLogger(
                TimedExecutionAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            service.execute("done");
            assertSame(target.failure, assertThrows(IllegalStateException.class,
                    service::fail));
            assertTrue(appender.list.stream().anyMatch(event -> event
                    .getFormattedMessage()
                    .equals("operation=test.execute result=done")));
            assertTrue(appender.list.stream().anyMatch(event -> event
                    .getFormattedMessage().equals("operation=test.fail failed")
                    && event.getThrowableProxy() != null));
            assertDuration("test.fail");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void canDisableLogsWithoutDisablingMetrics() {
        Logger logger = (Logger) LoggerFactory.getLogger(
                TimedExecutionAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            service.quiet();
            assertTrue(appender.list.isEmpty());
            assertDuration("test.quiet");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private void assertDuration(String operation) {
        var timer = registry.get(operation).timer();
        assertEquals(1, timer.count());
        assertEquals(Duration.ofMillis(7).toNanos(),
                timer.totalTime(TimeUnit.NANOSECONDS));
    }

    private ExampleService proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAspect(new TimedExecutionAspect(registry));
        return factory.getProxy();
    }

    /** Методы с разными сигнатурами для проверки универсальности аспекта. */
    public class ExampleService {
        private final RuntimeException failure =
                new IllegalStateException("unavailable");

        /** @return результат без профилирования */
        public String plain() {
            return "plain";
        }

        /**
         * Возвращает переданный объект без изменений.
         * @param result произвольный результат
         * @return исходный объект, включая null
         */
        @TimedExecution(operation = "test.execute")
        public Object execute(Object result) {
            clock.add(Duration.ofMillis(7));
            return result;
        }

        /** Выполняет операцию без возвращаемого значения. */
        @TimedExecution(operation = "test.run")
        public void run() {
            clock.add(Duration.ofMillis(7));
        }

        /** Завершает операцию исходным исключением. */
        @TimedExecution(operation = "test.fail")
        public void fail() {
            clock.add(Duration.ofMillis(7));
            throw failure;
        }

        /** Выполняет операцию без записи результата в лог. */
        @TimedExecution(operation = "test.quiet", logDetails = false)
        public void quiet() {
            clock.add(Duration.ofMillis(7));
        }
    }
}
