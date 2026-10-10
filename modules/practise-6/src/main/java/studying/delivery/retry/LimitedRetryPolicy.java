package studying.delivery.retry;

import java.time.Duration;
import studying.delivery.model.AttemptResult;

/**
 * Повторяет временные ошибки с линейной задержкой и лимитом попыток.
 * @param maxAttempts лимит попыток, включая первую
 * @param baseDelay положительная базовая задержка повтора
 */
public record LimitedRetryPolicy(int maxAttempts, Duration baseDelay)
        implements RetryPolicy {

    @Override
    public RetryDecision decide(int attemptsMade,
                                AttemptResult failure) {
        if (failure.kind() != AttemptResult.Kind.TEMPORARY_FAILURE
                || attemptsMade >= maxAttempts) {
            return new RetryDecision.Stop();
        }
        return new RetryDecision.RetryAfter(
                baseDelay.multipliedBy(attemptsMade));
    }
}
