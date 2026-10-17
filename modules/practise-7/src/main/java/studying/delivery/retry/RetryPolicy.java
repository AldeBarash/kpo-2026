package studying.delivery.retry;

import studying.delivery.model.AttemptResult;

/** Стратегия принятия решения о повторе после ошибки отправки. */
@FunctionalInterface
public interface RetryPolicy {
    /**
     * Принимает решение без изменения состояния, чтения часов и ожидания.
     * @param attemptsMade число попыток, включая только что выполненную
     * @param failure результат неудачной попытки
     * @return остановка или повтор с положительной задержкой
     */
    RetryDecision decide(int attemptsMade, AttemptResult failure);
}
