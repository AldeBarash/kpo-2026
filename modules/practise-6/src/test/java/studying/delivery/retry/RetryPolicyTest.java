package studying.delivery.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import studying.delivery.exception.DeliveryException;
import studying.delivery.mapper.RetryPolicyMapper;
import studying.delivery.model.AttemptResult;
import studying.delivery.validation.DeliveryValidation;
import studying.delivery.validation.RetryValidator;
import studying.exception.ApplicationErrorCode;

/** Модульные тесты решений политик повторов. */
@DisplayName("Политики повторов")
class RetryPolicyTest {
    private final RetryPolicyMapper mapper =
            new RetryPolicyMapper(new RetryValidator(new DeliveryValidation()));
    private static final AttemptResult TEMPORARY = new AttemptResult(
            AttemptResult.Kind.TEMPORARY_FAILURE, "Сервис недоступен");
    private static final AttemptResult PERMANENT = new AttemptResult(
            AttemptResult.Kind.PERMANENT_FAILURE, "Адрес некорректен");

    @Test
    @DisplayName("Без повторов любая ошибка прекращает доставку")
    void stopsWithoutRetries() {
        RetryPolicy policy = new NoRetryPolicy();
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(1, TEMPORARY));
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(1, PERMANENT));
    }

    @Test
    @DisplayName("Задержки равны 10 и 20 секундам, третья "
            + "ошибка завершает доставку")
    void appliesLinearDelaysAndLimit() {
        RetryPolicy policy = mapper.limited(3,
                Duration.ofSeconds(10));
        assertEquals(mapper.retryAfter(Duration.ofSeconds(10)),
                policy.decide(1, TEMPORARY));
        assertEquals(mapper.retryAfter(Duration.ofSeconds(20)),
                policy.decide(2, TEMPORARY));
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(3, TEMPORARY));
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(4, TEMPORARY));
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(1, PERMANENT));
    }

    @Test
    @DisplayName("Лимит один включает первую попытку")
    void countsFirstAttempt() {
        RetryPolicy policy = mapper.limited(1,
                Duration.ofSeconds(10));
        assertInstanceOf(RetryDecision.Stop.class, policy.decide(1, TEMPORARY));
    }

    @Test
    @DisplayName("Некорректные параметры политики и задержки отклоняются")
    void rejectsInvalidParameters() {
        for (int limit : new int[]{0, -1}) {
            assertCode(ApplicationErrorCode.INVALID_RETRY_POLICY,
                    () -> mapper.limited(limit, Duration.ofSeconds(1)));
        }
        for (Duration delay : new Duration[]{Duration.ZERO,
                Duration.ofSeconds(-1)}) {
            assertCode(ApplicationErrorCode.INVALID_RETRY_POLICY,
                    () -> mapper.limited(3, delay));
            assertCode(ApplicationErrorCode.INVALID_RETRY_DELAY,
                    () -> mapper.retryAfter(delay));
        }
        assertCode(ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                () -> mapper.limited(3, null));
        assertCode(ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                () -> mapper.retryAfter(null));
    }

    private static void assertCode(ApplicationErrorCode expected,
                                   Executable action) {
        assertEquals(expected,
                assertThrows(DeliveryException.class, action).getCode());
    }
}
