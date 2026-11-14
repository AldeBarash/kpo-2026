package studying.telegram.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.telegram")
public record TelegramProperties(@DefaultValue("false") boolean enabled,
        @DefaultValue("") String token,
        @DefaultValue("https://api.telegram.org") String baseUrl) {
    public TelegramProperties {
        if (enabled && (token == null || token.isBlank())) {
            throw new IllegalArgumentException("TELEGRAM_BOT_TOKEN is required when bot is enabled");
        }
    }
    @Override
    public String toString() { return "TelegramProperties[enabled=" + enabled + "]"; }
}
