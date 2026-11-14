package studying.telegram.delivery;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import studying.gateway.GatewayApplication;
import studying.gateway.service.*;
import studying.gateway.events.OutboxPublisher;
import studying.telegram.TelegramApplication;
import studying.telegram.bot.*;
import studying.telegram.events.*;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeliveryIntegrationTest {
    static final String TOKEN = "integration-test-service-token-at-least-32-characters";
    @Container static final PostgreSQLContainer GATEWAY_DB = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("gateway").withUsername("gateway").withPassword("gateway-test");
    @Container static final PostgreSQLContainer TELEGRAM_DB = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("telegram").withUsername("telegram").withPassword("telegram-test");
    @Container static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.1");

    ConfigurableApplicationContext gateway;
    ConfigurableApplicationContext telegram;
    JdbcTemplate gatewayDb;
    JdbcTemplate telegramDb;
    UserAccountService accounts;
    BillingService billing;
    TelegramUpdates updates;
    GatewayClient gatewayClient;
    DeliveryQueue telegramQueue;
    studying.gateway.events.DeliveryQueue outboxQueue;
    JsonMapper json;
    RestClient http;
    HttpServer ollama;
    UUID user;
    String apiKey;
    long updateId;

    @BeforeAll
    void startServices() throws Exception {
        ollama = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ollama.createContext("/api/chat", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = """
                    {"model":"qwen2.5:0.5b","created_at":"2026-11-14T10:00:00Z",
                     "message":{"role":"assistant","content":"Test answer"},"done":true,
                     "done_reason":"stop","prompt_eval_count":18,"eval_count":42}
                    """.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length); exchange.getResponseBody().write(body); exchange.close();
        });
        ollama.start();
        try (var admin = AdminClient.create(Map.of("bootstrap.servers", KAFKA.getBootstrapServers()))) {
            admin.createTopics(java.util.List.of(new NewTopic("billing.credits-charged.v1", 3, (short) 1),
                    new NewTopic("billing.credits-charged.dlt", 3, (short) 1))).all().get(20, TimeUnit.SECONDS);
        }
        gateway = new SpringApplicationBuilder(GatewayApplication.class).run(
                "--spring.config.name=gateway", "--server.port=0", "--app.workers.enabled=false",
                "--spring.datasource.url=" + GATEWAY_DB.getJdbcUrl(),
                "--spring.datasource.username=" + GATEWAY_DB.getUsername(),
                "--spring.datasource.password=" + GATEWAY_DB.getPassword(),
                "--spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                "--app.internal.token=" + TOKEN,
                "--spring.ai.ollama.base-url=http://127.0.0.1:" + ollama.getAddress().getPort());
        String gatewayUrl = "http://127.0.0.1:" + gateway.getEnvironment().getProperty("local.server.port");
        telegram = new SpringApplicationBuilder(TelegramApplication.class).run(
                "--spring.config.name=telegram", "--server.port=0", "--app.workers.enabled=false",
                "--app.telegram.enabled=false", "--app.gateway.base-url=" + gatewayUrl,
                "--app.gateway.token=" + TOKEN,
                "--spring.datasource.url=" + TELEGRAM_DB.getJdbcUrl(),
                "--spring.datasource.username=" + TELEGRAM_DB.getUsername(),
                "--spring.datasource.password=" + TELEGRAM_DB.getPassword(),
                "--spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                "--spring.autoconfigure.exclude=org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration");
        gatewayDb = gateway.getBean(JdbcTemplate.class); telegramDb = telegram.getBean(JdbcTemplate.class);
        accounts = gateway.getBean(UserAccountService.class); billing = gateway.getBean(BillingService.class);
        updates = telegram.getBean(TelegramUpdates.class); gatewayClient = telegram.getBean(GatewayClient.class);
        telegramQueue = telegram.getBean(DeliveryQueue.class);
        outboxQueue = gateway.getBean(studying.gateway.events.DeliveryQueue.class);
        json = telegram.getBean(JsonMapper.class); http = RestClient.create(gatewayUrl);
    }

    @AfterAll
    void stopServices() {
        if (telegram != null) { telegram.close(); }
        if (gateway != null) { gateway.close(); }
        if (ollama != null) { ollama.stop(0); }
    }

    @BeforeEach
    void createAccount() {
        user = accounts.register(UUID.randomUUID() + "@example.com").id();
        apiKey = accounts.issueApiKey(user).key(); accounts.topUp(user, 10000);
        updateId = Math.abs(UUID.randomUUID().getMostSignificantBits()) / 2;
    }

    @Test
    void serviceDatabasesAndHttpEndpointsAreIsolated() {
        assertThat(gatewayDb.queryForObject("SELECT to_regclass('telegram_bindings') IS NULL", Boolean.class)).isTrue();
        assertThat(gatewayDb.queryForObject("SELECT to_regclass('inbox_events') IS NULL", Boolean.class)).isTrue();
        assertThat(telegramDb.queryForObject("SELECT to_regclass('wallets') IS NULL", Boolean.class)).isTrue();
        assertThat(telegramDb.queryForObject("SELECT to_regclass('outbox_events') IS NULL", Boolean.class)).isTrue();
        assertThat(telegram.getBeansOfType(BillingService.class)).isEmpty();
        assertThat(gateway.getBeansOfType(TelegramUpdates.class)).isEmpty();
        assertThatThrownBy(() -> http.get().uri("/internal/users/" + user + "/wallet").retrieve().body(String.class))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void fullHttpBillingOutboxKafkaInboxAndTelegramDelivery() throws Exception {
        bind(701);
        // No pending command replies in this test's send queue.
        telegramDb.update("UPDATE telegram_notifications SET status='SENT', lock_token=NULL, locked_until=NULL");
        Map<String, Object> body = Map.of("model", "qwen2.5:0.5b", "prompt", "Hi", "maxTokens", 80);
        var response = json.readTree(http.post().uri("/v1/chat/completions").header("Authorization", "Bearer " + apiKey)
                .header("Idempotency-Key", "http-test").body(body).retrieve().body(String.class));
        UUID request = UUID.fromString(response.path("requestId").asText());
        assertThat(response.path("content").asText()).isEqualTo("Test answer");
        var event = event(request);
        assertThat(event.chargedCredits()).isEqualTo(11); assertThat(event.balanceAfter()).isEqualTo(9989);
        var repeat = json.readTree(http.post().uri("/v1/chat/completions").header("Authorization", "Bearer " + apiKey)
                .header("Idempotency-Key", "http-test").body(body).retrieve().body(String.class));
        assertThat(repeat.path("requestId").asText()).isEqualTo(request.toString());
        assertThat(gatewayDb.queryForObject("SELECT count(*) FROM outbox_events WHERE ai_request_id=?", Long.class, request)).isOne();
        publishUntilSent(event.eventId());
        await(() -> count("SELECT count(*) FROM telegram_notifications WHERE event_id=?", event.eventId()) == 1);
        kafka().send("billing.credits-charged.v1", user.toString(), json.writeValueAsString(event)).get(10, TimeUnit.SECONDS);
        telegram.getBean(NotificationInbox.class).accept(event);
        assertThat(count("SELECT count(*) FROM telegram_notifications WHERE event_id=?", event.eventId())).isOne();
        try (var stub = new TelegramStub(200)) {
            worker(stub).send();
            assertThat(stub.messages).hasSize(1);
            assertThat(stub.messages.getFirst()).contains(request.toString(), "9989");
        }
    }

    @Test
    void rollbackAndOllamaFailureDoNotLeaveChargeOrEvent() {
        var reservation = reserve();
        gateway.getBean(TransactionTemplate.class).executeWithoutResult(status -> {
            billing.complete(reservation.requestId(), 18, 42, "Answer"); status.setRollbackOnly();
        });
        assertThat(accounts.wallet(user).balance()).isEqualTo(10000);
        assertThat(gatewayDb.queryForObject("SELECT count(*) FROM outbox_events WHERE ai_request_id=?", Long.class, reservation.requestId())).isZero();
        assertThat(gatewayDb.queryForObject("SELECT count(*) FROM usage_records WHERE ai_request_id=?", Long.class, reservation.requestId())).isZero();
        billing.fail(reservation.requestId(), "TEST_FAILURE");
        assertThat(accounts.wallet(user).reservedBalance()).isZero();
    }

    @Test
    void redemptionSurvivesCrashBetweenRemoteCommitAndLocalCommit() {
        String code = code();
        UUID redeemed = gatewayClient.redeem(updateId, 801, 801, code);
        assertThat(redeemed).isEqualTo(user);
        // Gateway committed, but Telegram process has not stored the binding or update yet.
        assertThat(count("SELECT count(*) FROM telegram_bindings WHERE user_id=?", user)).isZero();
        updates.handle(update(updateId, 801, "/link " + code));
        updates.handle(update(updateId, 801, "/link " + code));
        assertThat(count("SELECT count(*) FROM telegram_bindings WHERE user_id=?", user)).isOne();
        assertThat(count("SELECT count(*) FROM telegram_notifications WHERE update_id=?", updateId)).isOne();
        assertThatThrownBy(() -> gatewayClient.redeem(updateId + 1, 802, 802, code)).isInstanceOf(GatewayClient.Rejected.class);
        assertThatThrownBy(() -> gatewayClient.redeem(updateId, 802, 802, code)).isInstanceOf(GatewayClient.Rejected.class);
    }

    @Test
    void balanceUsesGatewayHttpAndExpiredCodesAndGroupsCannotLink() {
        String expired = code();
        gatewayDb.update("UPDATE telegram_link_codes SET expires_at=now()-interval '1 second' WHERE user_id=?", user);
        updates.handle(update(updateId, 901, "/link " + expired));
        assertThat(count("SELECT count(*) FROM telegram_bindings WHERE user_id=?", user)).isZero();
        String valid = code();
        updates.handle(json.readTree(json.writeValueAsString(update(updateId + 1, 901, "/link " + valid)).replace("private", "supergroup")));
        assertThat(count("SELECT count(*) FROM telegram_bindings WHERE user_id=?", user)).isZero();
        updates.handle(update(updateId + 2, 901, "/link " + valid));
        updates.handle(update(updateId + 3, 901, "/balance"));
        assertThat(telegramDb.queryForObject("SELECT payload->>'text' FROM telegram_notifications WHERE update_id=?", String.class, updateId + 3))
                .contains("10000", "Доступно");
    }

    @Test
    void concurrentCodeRedemptionHasOneWinner() throws Exception {
        String code = code();
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var gate = new java.util.concurrent.CountDownLatch(1);
            var first = pool.submit(() -> { gate.await(); updates.handle(update(updateId, 1001, "/link " + code)); return null; });
            var second = pool.submit(() -> { gate.await(); updates.handle(update(updateId + 1, 1002, "/link " + code)); return null; });
            gate.countDown(); first.get(10, TimeUnit.SECONDS); second.get(10, TimeUnit.SECONDS);
        }
        assertThat(count("SELECT count(*) FROM telegram_bindings WHERE user_id=?", user)).isOne();
    }

    @Test
    void leaseRecoveryRejectsStaleWorkerAndExhaustedJobsStopRetrying() {
        clearOutbox(); var reservation = reserve(); billing.complete(reservation.requestId(), 18, 42, "Answer");
        var stale = outboxQueue.claim().getFirst();
        gatewayDb.update("UPDATE outbox_events SET locked_until=now()-interval '1 second', attempts=7 WHERE id=?", stale.id());
        var recovered = outboxQueue.claim().getFirst();
        assertThat(recovered.payload()).isEqualTo(stale.payload()); assertThat(recovered.token()).isNotEqualTo(stale.token());
        outboxQueue.sent(stale);
        assertThat(gatewayDb.queryForObject("SELECT status FROM outbox_events WHERE id=?", String.class, stale.id())).isEqualTo("PROCESSING");
        outboxQueue.failed(recovered, "KAFKA_SEND_FAILED", false, 0);
        assertThat(gatewayDb.queryForObject("SELECT status FROM outbox_events WHERE id=?", String.class, stale.id())).isEqualTo("FAILED");
        assertThat(outboxQueue.claim()).isEmpty();
    }

    @Test
    void kafkaOutageDoesNotRollBackBillingAndRecoveryPublishes() throws Exception {
        clearOutbox(); var reservation = reserve();
        KAFKA.getDockerClient().pauseContainerCmd(KAFKA.getContainerId()).exec();
        try {
            billing.complete(reservation.requestId(), 18, 42, "Answer"); gateway.getBean(OutboxPublisher.class).publish();
            assertThat(accounts.wallet(user).balance()).isEqualTo(9989);
            assertThat(gatewayDb.queryForObject("SELECT status FROM outbox_events WHERE ai_request_id=?", String.class, reservation.requestId())).isEqualTo("NEW");
        } finally { KAFKA.getDockerClient().unpauseContainerCmd(KAFKA.getContainerId()).exec(); }
        gatewayDb.update("UPDATE outbox_events SET next_attempt_at=now() WHERE ai_request_id=?", reservation.requestId());
        publishUntilSent(event(reservation.requestId()).eventId());
    }

    @Test
    void telegramRateLimitAndUnlinkPreventSendingToOldBinding() throws Exception {
        bind(1101); clearNotifications();
        var reservation = reserve(); billing.complete(reservation.requestId(), 18, 42, "Answer"); var event = event(reservation.requestId());
        telegram.getBean(NotificationInbox.class).accept(event);
        try (var stub = new TelegramStub(429)) {
            worker(stub).send();
            assertThat(telegramDb.queryForObject("SELECT next_attempt_at>now()+interval '10 seconds' FROM telegram_notifications WHERE event_id=?", Boolean.class, event.eventId())).isTrue();
            updates.handle(update(updateId + 1, 1101, "/unlink"));
            telegramDb.update("UPDATE telegram_notifications SET status='SENT' WHERE update_id IS NOT NULL");
            telegramDb.update("UPDATE telegram_notifications SET next_attempt_at=now() WHERE event_id=?", event.eventId());
            worker(stub).send();
            assertThat(stub.messages).hasSize(1);
            assertThat(telegramDb.queryForObject("SELECT status FROM telegram_notifications WHERE event_id=?", String.class, event.eventId())).isEqualTo("SKIPPED");
        }
    }

    @Test
    void externalTelegramSuccessBeforeCrashCanBeRepeatedWithoutNewJob() throws Exception {
        bind(1201); clearNotifications(); var reservation = reserve(); billing.complete(reservation.requestId(), 18, 42, "Answer");
        var event = event(reservation.requestId()); telegram.getBean(NotificationInbox.class).accept(event);
        try (var stub = new TelegramStub(200)) {
            var job = telegramQueue.claim().getFirst();
            stub.client().send(job.chatId(), json.readValue(job.payload(), NotificationInbox.Message.class).text());
            telegramDb.update("UPDATE telegram_notifications SET locked_until=now()-interval '1 second' WHERE id=?", job.id());
            worker(stub).send();
            assertThat(stub.messages).hasSize(2);
            assertThat(count("SELECT count(*) FROM telegram_notifications WHERE event_id=?", event.eventId())).isOne();
        }
    }

    @Test
    void malformedEventGoesToDltAndFollowingValidEventIsProcessed() throws Exception {
        try (var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(Map.of(
                "bootstrap.servers", KAFKA.getBootstrapServers(), "group.id", UUID.randomUUID().toString(),
                "auto.offset.reset", "earliest", "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"))) {
            consumer.subscribe(java.util.List.of("billing.credits-charged.dlt"));
            kafka().send("billing.credits-charged.v1", user.toString(), "{broken").get(10, TimeUnit.SECONDS);
            long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos(); boolean found = false;
            while (!found && System.nanoTime() < deadline) {
                for (var record : consumer.poll(Duration.ofMillis(200))) { if (record.value().equals("{broken")) { found = true; } }
            }
            assertThat(found).isTrue();
            var reservation = reserve(); billing.complete(reservation.requestId(), 18, 42, "Answer"); var event = event(reservation.requestId());
            kafka().send("billing.credits-charged.v1", user.toString(), json.writeValueAsString(event)).get(10, TimeUnit.SECONDS);
            await(() -> count("SELECT count(*) FROM inbox_events WHERE event_id=?", event.eventId()) == 1);
        }
    }

    private String code() {
        return json.readTree(http.post().uri("/api/telegram/link-codes").header("Authorization", "Bearer " + apiKey)
                .retrieve().body(String.class)).path("code").asText();
    }
    private void bind(long sender) { updates.handle(update(updateId, sender, "/link " + code())); }
    private BillingService.Reservation reserve() { return billing.reserve(apiKey, "request", "hash", "qwen2.5:0.5b", "Hi", 80); }
    private CreditsCharged event(UUID request) {
        return json.readValue(gatewayDb.queryForObject("SELECT payload::text FROM outbox_events WHERE ai_request_id=?", String.class, request), CreditsCharged.class);
    }
    private tools.jackson.databind.JsonNode update(long id, long sender, String text) {
        return json.valueToTree(Map.of("update_id", id, "message", Map.of("from", Map.of("id", sender),
                "chat", Map.of("id", sender, "type", "private"), "text", text)));
    }
    @SuppressWarnings("unchecked") private KafkaTemplate<String, String> kafka() { return telegram.getBean(KafkaTemplate.class); }
    private long count(String sql, Object... args) { return telegramDb.queryForObject(sql, Long.class, args); }
    private void clearOutbox() { gatewayDb.update("UPDATE outbox_events SET status='SENT', lock_token=NULL, locked_until=NULL"); }
    private void clearNotifications() { telegramDb.update("UPDATE telegram_notifications SET status='SENT', lock_token=NULL, locked_until=NULL"); }
    private void publishUntilSent(UUID id) throws Exception {
        await(() -> { gateway.getBean(OutboxPublisher.class).publish(); return "SENT".equals(gatewayDb.queryForObject("SELECT status FROM outbox_events WHERE id=?", String.class, id)); });
    }
    private void await(java.util.concurrent.Callable<Boolean> condition) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while (System.nanoTime() < deadline) { if (condition.call()) { return; } Thread.sleep(50); }
        assertThat(condition.call()).isTrue();
    }
    private TelegramNotificationWorker worker(TelegramStub stub) {
        return new TelegramNotificationWorker(stub.settings(), telegramQueue, stub.client(), telegramDb, json);
    }
    class TelegramStub implements AutoCloseable {
        final HttpServer server;
        final java.util.List<String> messages = new java.util.ArrayList<>();
        TelegramStub(int status) throws Exception {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/bottest-only/sendMessage", exchange -> {
                messages.add(new String(exchange.getRequestBody().readAllBytes()));
                byte[] response = (status == 200 ? "{\"ok\":true,\"result\":{\"message_id\":1}}"
                        : "{\"ok\":false,\"error_code\":429,\"parameters\":{\"retry_after\":12}}").getBytes();
                exchange.sendResponseHeaders(status, response.length); exchange.getResponseBody().write(response); exchange.close();
            }); server.start();
        }
        TelegramProperties settings() { return new TelegramProperties(true, "test-only", "http://127.0.0.1:" + server.getAddress().getPort()); }
        TelegramClient client() { return new TelegramClient(settings(), json); }
        public void close() { server.stop(0); }
    }
}
