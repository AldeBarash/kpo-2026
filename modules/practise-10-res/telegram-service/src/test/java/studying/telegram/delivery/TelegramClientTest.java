package studying.telegram.delivery;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import studying.telegram.bot.TelegramClient;
import studying.telegram.bot.TelegramProperties;
import tools.jackson.databind.json.JsonMapper;

class TelegramClientTest {
    @Test
    void permanentErrorsAreSafeAndRateLimitRetainsRetryAfter() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var status = new java.util.concurrent.atomic.AtomicInteger(403);
        server.createContext("/botsecret-test-token/sendMessage", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = ("{\"ok\":false,\"error_code\":" + status.get()
                    + ",\"description\":\"secret-test-token\",\"parameters\":{\"retry_after\":15}}").getBytes();
            exchange.sendResponseHeaders(status.get(), body.length); exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        try {
            var properties = new TelegramProperties(true, "secret-test-token", "http://127.0.0.1:" + server.getAddress().getPort());
            var client = new TelegramClient(properties, JsonMapper.builder().build());
            assertThat(properties.toString()).doesNotContain("secret-test-token");
            var permanent = catchThrowableOfType(TelegramClient.Failure.class, () -> client.send(1, "Hello"));
            assertThat(permanent.permanent()).isTrue();
            assertThat(permanent).hasMessage("TELEGRAM_403").hasNoCause();
            status.set(429);
            var limited = catchThrowableOfType(TelegramClient.Failure.class, () -> client.send(1, "Hello"));
            assertThat(limited.permanent()).isFalse();
            assertThat(limited.retryAfter()).isEqualTo(15);
        } finally { server.stop(0); }
    }

    @Test
    void disabledBotNeedsNoTokenAndEnabledBotRequiresIt() {
        assertThatCode(() -> new TelegramProperties(false, "", "https://api.telegram.org")).doesNotThrowAnyException();
        assertThatThrownBy(() -> new TelegramProperties(true, "", "https://api.telegram.org"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
