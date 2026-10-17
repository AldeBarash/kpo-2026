package studying.delivery.validation;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.exception.DeliveryException;
import studying.exception.ApplicationErrorCode;

/** Проверки параметров политик и задержек повторов. */
@Component
@RequiredArgsConstructor
public final class RetryValidator {
    private final DeliveryValidation validation;

    /**
     * Проверяет параметры повторной отправки.
     * @param maxAttempts лимит попыток
     * @param baseDelay базовая задержка
     */
    public void validatePolicy(int maxAttempts, Duration baseDelay) {
        validation.required(baseDelay,
                "Базовая задержка обязательна");
        if (maxAttempts < 1 || baseDelay.isZero() || baseDelay.isNegative()) {
            throw new DeliveryException(
                    ApplicationErrorCode.INVALID_RETRY_POLICY,
                    "Лимит должен быть >= 1, задержка должна быть > 0");
        }
    }

    /**
     * Проверяет параметры повторной отправки.
     * @param delay задержка повтора
     */
    public void validateDelay(Duration delay) {
        validation.required(delay,
                "Задержка повтора обязательна");
        if (delay.isZero() || delay.isNegative()) {
            throw new DeliveryException(
                    ApplicationErrorCode.INVALID_RETRY_DELAY,
                    "Задержка должна быть > 0");
        }
    }
}
