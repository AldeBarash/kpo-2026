package studying;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Запускает HTTP API доставки уведомлений. */
@SpringBootApplication(proxyBeanMethods = false)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Main {
    /**
     * Запускает сервер и собирает зависимости приложения.
     * @param args аргументы запуска
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
