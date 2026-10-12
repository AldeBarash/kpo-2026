package studying.delivery.mapper;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.retry.LimitedRetryPolicy;
import studying.delivery.validation.RetryValidator;

/** Создаёт проверенные политики повторных попыток. */
@Component
@RequiredArgsConstructor
public final class RetryPolicyMapper {
    private final RetryValidator validator;

    /**
     * Проверяет параметры и создаёт ограниченную политику повторов.
     * @return проверенная политика повторов
     */
    public LimitedRetryPolicy limited(int maxAttempts, Duration baseDelay) {
        validator.validatePolicy(maxAttempts, baseDelay);
        return new LimitedRetryPolicy(maxAttempts, baseDelay);
    }

    /**
     * Проверяет задержку и создаёт решение о повторе.
     * @return решение назначить повтор
     */
    public studying.delivery.retry.RetryDecision.RetryAfter retryAfter(
            Duration delay) {
        validator.validateDelay(delay);
        return new studying.delivery.retry.RetryDecision.RetryAfter(delay);
    }
}
