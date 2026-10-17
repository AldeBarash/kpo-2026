package studying;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import studying.profiling.TimedExecution;

/** Запускает пример доставки уведомлений. */
@SpringBootApplication(proxyBeanMethods = false)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Main {

    /**
     * Запускает приложение и демонстрирует доставку с повторами.
     * @param args аргументы запуска приложения
     */
    public static void main(String[] args) {
        var context = SpringApplication.run(Main.class, args);
        AnnotationDemo demo = context.getBean(AnnotationDemo.class);
        for (int attempt = 1; attempt <= 5; attempt++) {
            demo.execute("Вызов " + attempt);
        }
    }

    /** Демонстрирует профилирование обычного метода через Spring-прокси. */
    @Component
    public static class AnnotationDemo {
        /**
         * Возвращает результат, который аспект запишет в лог.
         * @param message сообщение демонстрационного вызова
         * @return результат выполнения
         */
        @TimedExecution(operation = "demo.annotation")
        public String execute(String message) {
            return "Обработано: " + message;
        }
    }
}
