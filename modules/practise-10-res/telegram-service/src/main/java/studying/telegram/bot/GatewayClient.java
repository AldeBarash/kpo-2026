package studying.telegram.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GatewayClient {
    private final String baseUrl;
    private final String token;
    private final JsonMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public GatewayClient(@Value("${app.gateway.base-url}") String baseUrl,
            @Value("${app.gateway.token}") String token, JsonMapper json) {
        if (token.length() < 32) { throw new IllegalArgumentException("Internal token must have at least 32 characters"); }
        this.baseUrl = baseUrl;
        this.token = token;
        this.json = json;
    }

    public UUID redeem(long updateId, long sender, long chat, String code) {
        var result = call("/internal/telegram/link-redemptions", Map.of("updateId", updateId,
                "telegramUserId", sender, "chatId", chat, "code", code));
        return UUID.fromString(result.path("userId").asText());
    }

    public String balance(UUID userId) {
        var wallet = call("/internal/users/" + userId + "/wallet", null);
        return "Баланс: " + wallet.path("balance").asLong() + ". Резерв: "
                + wallet.path("reservedBalance").asLong() + ". Доступно: "
                + wallet.path("availableBalance").asLong() + " кредитов.";
    }

    private JsonNode call(String path, Object body) {
        try {
            var builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(Duration.ofSeconds(5))
                    .header("X-Service-Token", token).header("Content-Type", "application/json");
            if (body == null) { builder.GET(); }
            else { builder.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))); }
            var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 400 || response.statusCode() == 409) {
                throw new Rejected("Неверный, просроченный или уже использованный код.");
            }
            if (response.statusCode() == 403 || response.statusCode() == 404) {
                throw new Rejected("Нет доступа к аккаунту. Проверьте привязку.");
            }
            if (response.statusCode() != 200) { throw new Unavailable(); }
            return json.readTree(response.body());
        } catch (Rejected | Unavailable exception) { throw exception; }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new Unavailable(); }
        catch (Exception exception) { throw new Unavailable(); }
    }

    public static class Rejected extends RuntimeException {
        public Rejected(String message) { super(message); }
    }
    public static class Unavailable extends RuntimeException {
        public Unavailable() { super("GATEWAY_UNAVAILABLE"); }
    }
}
