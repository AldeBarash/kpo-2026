package studying.delivery.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import studying.Main;
import studying.delivery.model.AttemptResult;
import studying.delivery.mapper.DeliveryMapper;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.retry.LimitedRetryPolicy;
import studying.delivery.sender.InMemoryNotificationSender;
import studying.delivery.service.DeliveryService;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.SenderValidator;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.factory.impl.EmailNotificationFactory;
import studying.notification.model.NotificationDraft;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Проверяет публикацию статистики настоящих попыток через HTTP Actuator. */
@SpringBootTest(classes = Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Метрики доставки в Actuator")
class DeliveryMetricsIntegrationTest {
    @Autowired
    private DeliveryStatistics statistics;

    @Autowired
    private DeliveryValidation validation;

    @Autowired
    private SenderValidator senderValidator;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private EmailNotificationFactory factory;

    @Value("${local.server.port}")
    private int port;

    @Test
    @DisplayName("Метрики учитывают попытки и доступны по адресу /metrics")
    void exposesAttemptCounters() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            assertTrue(get(client, "/metrics").get("names").toString()
                    .contains("delivery.attempts"));
            assertCount(client, "success", 0);
            assertCount(client, "temporary_failure", 0);
            assertCount(client, "permanent_failure", 0);
            InMemoryNotificationSender sender = new InMemoryNotificationSender(
                    List.of(new AttemptResult(
                                    AttemptResult.Kind.TEMPORARY_FAILURE,
                                    "Сервис недоступен"),
                            new AttemptResult(AttemptResult.Kind.SUCCESS, null),
                            new AttemptResult(
                                    AttemptResult.Kind.PERMANENT_FAILURE,
                                    "Некорректный адрес")),
                    validation, senderValidator);
            DeliveryService service = new DeliveryService(sender,
                    new DeliveryRepository(), List.of(statistics),
                    deliveryMapper, validation, senderValidator);
            NotificationDraft draft = NotificationDraft.builder()
                    .channel(NotificationChannel.EMAIL)
                    .recipient("student@example.org")
                    .text("Заказ готов")
                    .createdAt(LocalDateTime.parse("2026-10-01T10:00:00"))
                    .priority(NotificationPriority.NORMAL)
                    .build();
            var notification = factory.create(draft);
            var policy = new LimitedRetryPolicy(3, Duration.ofSeconds(10));
            service.enqueue(notification, policy, Instant.EPOCH);
            service.processDue(Instant.EPOCH);
            service.processDue(Instant.EPOCH.plusSeconds(9));
            assertCount(client, "temporary_failure", 1);
            assertCount(client, "success", 0);
            service.processDue(Instant.EPOCH.plusSeconds(10));
            service.processDue(Instant.EPOCH.plusSeconds(20));
            service.enqueue(notification, policy,
                    Instant.EPOCH.plusSeconds(30));
            service.processDue(Instant.EPOCH.plusSeconds(30));
            service.processDue(Instant.EPOCH.plusSeconds(60));
            assertCount(client, "success", 1);
            assertCount(client, "temporary_failure", 1);
            assertCount(client, "permanent_failure", 1);
            assertEquals(3.0, get(client, "/metrics/delivery.attempts")
                    .get("measurements").get(0).get("value").asDouble());
            assertEquals(1, statistics.successfulAttempts());
            assertEquals(1, statistics.temporaryFailures());
            assertEquals(1, statistics.permanentFailures());
        }
    }

    private void assertCount(HttpClient client, String result, double expected)
            throws Exception {
        JsonNode metric = get(client,
                "/metrics/delivery.attempts?tag=result:" + result);
        assertEquals("COUNT", metric.get("measurements").get(0)
                .get("statistic").asString());
        assertEquals(expected, metric.get("measurements").get(0)
                .get("value").asDouble());
    }

    private JsonNode get(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return new ObjectMapper().readTree(response.body());
    }
}
