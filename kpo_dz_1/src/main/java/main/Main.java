package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Точка входа: поднимает DI-контейнер Spring, затем отрабатывает
 * {@link ConsoleMenu}.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"impl", "service", "main"})
public class Main {
    /**
     * Защищённый конструктор: класс только запускает приложение.
     */
    protected Main() {
    }

    /**
     * Запускает приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(final String[] args) {
        SpringApplication app = new SpringApplication(Main.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
