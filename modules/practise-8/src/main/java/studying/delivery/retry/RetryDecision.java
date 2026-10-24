package studying.delivery.retry;

import java.time.Duration;

/** Решение политики: прекратить доставку или назначить повтор. */
public interface RetryDecision {
    /** Прекратить обработку доставки. */
    record Stop() implements RetryDecision { }

    /**
     * Назначить следующую попытку через положительный интервал времени.
     * @param delay строго положительная задержка повтора
     */
    record RetryAfter(Duration delay) implements RetryDecision { }
}
