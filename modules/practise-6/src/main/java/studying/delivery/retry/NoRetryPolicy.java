package studying.delivery.retry;

import studying.delivery.model.AttemptResult;

/** Разрешает только первую попытку отправки. */
public final class NoRetryPolicy implements RetryPolicy {
    @Override
    public RetryDecision decide(int attemptsMade,
                                AttemptResult failure) {
        return new RetryDecision.Stop();
    }
}
