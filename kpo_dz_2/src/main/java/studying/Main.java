package studying;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import studying.infrastructure.config.AppConfig;
import studying.infrastructure.console.ConsoleMenu;

/** Точка входа: поднимает DI-контейнер и запускает консоль. */
public final class Main {
    private Main() {
    }
    /**
     * Запускает приложение.
     *
     * @param args - аргументы командной строки (не используются)
     */
    public static void main(final String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class)) {
            context.getBean(ConsoleMenu.class).run();
        }
    }
}
