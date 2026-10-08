package studying.notification;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication // Включает автоконфигурацию и сканирование пакета
public class Main implements CommandLineRunner {

    // Spring автоматически внедрит сюда все три фабрики.
    // Ключами в Map станут строки "EMAIL", "PUSH", "IN_APP" (из аннотаций @Component)
    private final Map<String, NotificationFactory> factoryRegistry;

    public Main(Map<String, NotificationFactory> factoryRegistry) {
        this.factoryRegistry = factoryRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args); // Запуск Spring Boot приложения
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== СЕРВИС УВЕДОМЛЕНИЙ НА SPRING BOOT ЗАПУЩЕН ===");

        // 1. Создаем черновик с помощью Builder (Часть 1 задания)
        NotificationDraft draft = NotificationDraft.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("professor@university.edu")
                .text("Практика 5 выполнена успешно и интегрирована со Spring Boot!")
                .createdAt(LocalDateTime.now())
                .priority(Priority.CRITICAL)
                .build();

        // 2. Динамически выбираем фабрику и создаем уведомление через общий контракт (Часть 2 задания)
        String channelKey = draft.getChannel().name(); // Получим строку "EMAIL"
        NotificationFactory factory = factoryRegistry.get(channelKey);

        if (factory != null) {
            Notification notification = factory.create(draft);

            System.out.println("Успешно создано уведомление типа: " + notification.getClass().getSimpleName());
            System.out.println("Текст: " + notification.getText());
            System.out.println("Детали доставки: " + notification.getDeliveryDetails());
        } else {
            System.out.println("Фабрика для канала " + channelKey + " не найдена!");
        }

        System.out.println("=================================================");
    }
}
