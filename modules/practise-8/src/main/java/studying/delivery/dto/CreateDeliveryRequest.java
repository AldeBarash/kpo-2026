package studying.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;

/**
 * HTTP-запрос создания доставки.
 * @param channel канал
 * @param recipient получатель
 * @param text сообщение
 * @param priority приоритет
 * @param readyAt готовность первой попытки
 * @param retry настройки повторов
 */
public record CreateDeliveryRequest(
        @NotNull NotificationChannel channel,
        @NotBlank String recipient,
        @NotBlank String text,
        @NotNull NotificationPriority priority,
        @NotNull Instant readyAt,
        @NotNull @Valid RetryRequest retry) {
    /**
     * Обязательные параметры повторов; null отличается от нуля.
     * @param maxAttempts лимит, включая первую попытку
     * @param baseDelaySeconds положительная задержка в секундах
     */
    public record RetryRequest(@NotNull @Positive Integer maxAttempts,
            @NotNull @Positive Long baseDelaySeconds) { }
}
