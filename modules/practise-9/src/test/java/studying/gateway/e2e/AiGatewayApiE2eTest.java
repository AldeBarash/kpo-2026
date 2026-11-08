package studying.gateway.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Epic("AI Gateway")
@Feature("Биллинг запросов к Ollama")
@Owner("KPO course")
@EnabledIfSystemProperty(named = "e2e.base-url", matches = ".+")
class AiGatewayApiE2eTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("e2e.base-url");
    }

    @Test
    @Story("Фактическое списание кредитов")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Пользователь регистрируется, пополняет баланс и получает ответ Ollama. "
            + "Реальный usage сохраняется, резерв освобождается, стоимость списывается.")
    void chargesCreditsUsingRealOllamaUsage() throws Exception {
        TestAccount account = createFundedAccount(10_000);

        JsonNode completion = chat(account.apiKey(), UUID.randomUUID().toString(),
                "Ответь одним словом: столица Франции?", 30, 200);

        assertTrue(completion.at("/usage/promptTokens").asInt() > 0);
        assertTrue(completion.at("/usage/completionTokens").asInt() > 0);
        assertTrue(completion.at("/usage/chargedCredits").asLong() > 0);
        assertTrue(completion.get("balance").asLong() < 10_000);

        JsonNode wallet = get("/api/users/" + account.userId() + "/wallet", 200);
        assertEquals(0, wallet.get("reservedBalance").asLong());
        assertEquals(completion.get("balance").asLong(), wallet.get("balance").asLong());

        JsonNode transactions = get("/api/users/" + account.userId()
                + "/wallet/transactions?size=20", 200);
        String transactionJson = transactions.get("content").toString();
        assertTrue(transactionJson.contains("TOP_UP"));
        assertTrue(transactionJson.contains("RESERVE"));
        assertTrue(transactionJson.contains("CHARGE"));
        assertTrue(transactionJson.contains("RELEASE"));
    }

    @Test
    @Story("Идемпотентность")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Повтор с тем же телом и Idempotency-Key возвращает сохранённый ответ "
            + "и не списывает кредиты повторно.")
    void returnsCachedCompletionWithoutSecondCharge() throws Exception {
        TestAccount account = createFundedAccount(1_000);
        String idempotencyKey = UUID.randomUUID().toString();

        JsonNode first = chat(account.apiKey(), idempotencyKey,
                "Напиши число два цифрой.", 10, 200);
        JsonNode second = chat(account.apiKey(), idempotencyKey,
                "Напиши число два цифрой.", 10, 200);

        assertEquals(first.get("requestId").asText(), second.get("requestId").asText());
        assertEquals(first.get("balance").asLong(), second.get("balance").asLong());
        assertEquals(first.get("content").asText(), second.get("content").asText());
    }

    @Test
    @Story("Недостаточный баланс")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Запрос без пополнения отклоняется до обращения к Ollama.")
    void rejectsRequestWhenCreditsAreInsufficient() throws Exception {
        JsonNode user = post("/api/users",
                "{\"email\":\"empty-" + UUID.randomUUID() + "@example.com\"}", null, null, 201);
        String userId = user.get("id").asText();
        JsonNode key = post("/api/users/" + userId + "/api-keys", null,
                null, null, 201);

        JsonNode error = chat(key.get("key").asText(), UUID.randomUUID().toString(),
                "Этот запрос не должен дойти до модели", 100, 402);

        assertEquals("INSUFFICIENT_CREDITS", error.get("code").asText());
    }

    private TestAccount createFundedAccount(long amount) throws Exception {
        JsonNode user = post("/api/users",
                "{\"email\":\"allure-" + UUID.randomUUID() + "@example.com\"}",
                null, null, 201);
        String userId = user.get("id").asText();
        post("/api/users/" + userId + "/top-ups",
                "{\"amount\":" + amount + "}", null, null, 200);
        JsonNode key = post("/api/users/" + userId + "/api-keys",
                null, null, null, 201);
        return new TestAccount(userId, key.get("key").asText());
    }

    private JsonNode chat(String apiKey, String idempotencyKey, String prompt,
                          int maxTokens, int expectedStatus) throws Exception {
        String body = objectMapper.writeValueAsString(new ChatBody(
                "qwen2.5:0.5b", prompt, maxTokens));
        return post("/v1/chat/completions", body, apiKey, idempotencyKey, expectedStatus);
    }

    @Step("GET {path}, ожидается HTTP {expectedStatus}")
    private JsonNode get(String path, int expectedStatus) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return send(request, expectedStatus);
    }

    @Step("POST {path}, ожидается HTTP {expectedStatus}")
    private JsonNode post(String path, String body, String apiKey,
                          String idempotencyKey, int expectedStatus) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(120));
        if (body == null) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
        }
        if (apiKey != null) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        return send(builder.build(), expectedStatus);
    }

    private JsonNode send(HttpRequest request, int expectedStatus)
            throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        Allure.getLifecycle().addAttachment("HTTP response " + response.statusCode(),
                "application/json", ".json",
                response.body().getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedStatus, response.statusCode(), response.body());
        return objectMapper.readTree(response.body());
    }

    private record TestAccount(String userId, String apiKey) {
    }

    private record ChatBody(String model, String prompt, int maxTokens) {
    }
}
