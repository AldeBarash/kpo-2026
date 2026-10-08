package studying.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Поднимает контекст Spring Boot для теста
@DisplayName("Интеграционные тесты со Spring Boot и @Autowired")
class NotificationIntegrationTest {

    // Spring Boot автоматически найдет все @Service фабрики
    // и внедрит их в эту карту по их именам ("EMAIL", "PUSH", "IN_APP")
    @Autowired
    private Map<String, NotificationFactory> factoryRegistry;

    @Test
    @DisplayName("Проверка автосвязывания фабрик и запуска создания уведомления")
    void integrationScenarioTest() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 10, 3, 14, 0);

        // 1. Создаем черновик (Часть 1 задания)
        NotificationDraft draft = NotificationDraft.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("professor@university.edu")
                .text("Практика 5 успешно проверена через @Autowired!")
                .createdAt(fixedTime)
                .priority(Priority.HIGH)
                .build();

        // 2. Получаем нужную фабрику из внедренной Spring-ом карты
        String channelKey = draft.getChannel().name(); // Получим строку "EMAIL"
        NotificationFactory factory = factoryRegistry.get(channelKey);

        // Проверяем, что @Autowired успешно сработал и фабрика внедрилась
        assertNotNull(factory, "Spring Boot должен был внедрить фабрику для канала " + channelKey);

        // 3. Запускаем создание уведомления через контракт фабрики (Часть 2 задания)
        Notification notification = factory.create(draft);

        // 4. Проверяем итоговый результат
        assertEquals("Практика 5 успешно проверена через @Autowired!", notification.getText());
        assertTrue(notification instanceof EmailNotification);
        assertEquals("Email to: professor@university.edu, Subject: Уведомление магазина", notification.getDeliveryDetails());
    }
}
