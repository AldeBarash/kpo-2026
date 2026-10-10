package studying;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Запускает пример доставки уведомлений. */
@SpringBootApplication(proxyBeanMethods = false)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Main {

    /**
     * Запускает приложение и демонстрирует доставку с повторами.
     * @param args аргументы запуска приложения
     */
    static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
