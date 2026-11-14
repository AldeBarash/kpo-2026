package studying.telegram.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class TelegramClient {
    private final TelegramProperties properties;
    private final JsonMapper json;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();

    public TelegramClient(TelegramProperties properties, JsonMapper json) {
        this.properties = properties;
        this.json = json;
    }

    public JsonNode updates(long offset) {
        return call("getUpdates", Map.of("offset", offset, "timeout", 20,
                "allowed_updates", new String[]{"message"}), Duration.ofSeconds(25));
    }

    public void send(long chatId, String text) {
        call("sendMessage", Map.of("chat_id", chatId, "text", text), Duration.ofSeconds(10));
    }

    private JsonNode call(String method, Object body, Duration timeout) {
        if (!properties.enabled()) { throw new Failure(false, 0, "BOT_DISABLED"); }
        try {
            var request = HttpRequest.newBuilder(URI.create(properties.baseUrl() + "/bot"
                            + properties.token() + "/" + method))
                    .timeout(timeout).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode result = json.readTree(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300
                    && result.path("ok").asBoolean(false)) {
                return result.path("result");
            }
            int code = result.path("error_code").asInt(response.statusCode());
            long retryAfter = result.path("parameters").path("retry_after").asLong(0);
            throw new Failure(code >= 400 && code < 500 && code != 429,
                    retryAfter, "TELEGRAM_" + code);
        } catch (Failure failure) {
            throw failure;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new Failure(false, 0, "TELEGRAM_INTERRUPTED");
        } catch (Exception exception) {
            // Never propagate the request URI or the raw response: the URI contains a secret.
            throw new Failure(false, 0, "TELEGRAM_NETWORK_ERROR");
        }
    }

    public static class Failure extends RuntimeException {
        private final boolean permanent;
        private final long retryAfter;
        public Failure(boolean permanent, long retryAfter, String safeCode) {
            super(safeCode);
            this.permanent = permanent;
            this.retryAfter = retryAfter;
        }
        public boolean permanent() { return permanent; }
        public long retryAfter() { return retryAfter; }
    }
}
