package studying.profiling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Включает лог результата и метрику времени метода Spring-компонента. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TimedExecution {
    /** @return имя таймера Micrometer */
    String operation() default "notification.send";

    /** @return нужно ли записывать результат и исключение в лог */
    boolean logDetails() default true;
}
