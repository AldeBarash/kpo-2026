package studying.telegram.events;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.delivery")
public record DeliveryProperties(
        @DefaultValue("billing.credits-charged.v1") String topic,
        @DefaultValue("billing.credits-charged.dlt") String dlt,
        @DefaultValue("8") int maxAttempts,
        @DefaultValue("60") int leaseSeconds,
        @DefaultValue("2") int retrySeconds,
        @DefaultValue("300") int maxRetrySeconds) {
    public DeliveryProperties {
        if (maxAttempts < 1 || leaseSeconds < 30 || retrySeconds < 1
                || maxRetrySeconds < retrySeconds) {
            throw new IllegalArgumentException("Invalid delivery settings");
        }
    }

    public long delay(int attempts) {
        return Math.min(maxRetrySeconds, (long) retrySeconds << Math.min(attempts - 1, 20));
    }
}
