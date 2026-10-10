package studying.delivery.validation;

import java.util.Collection;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.exception.DeliveryException;
import studying.delivery.model.AttemptResult;
import studying.exception.ApplicationErrorCode;

/** Проверки состояния и результата отправителя. */
@Component
@NoArgsConstructor
public final class SenderValidator {
    /**
     * Проверяет наличие результата отправки.
     * @param result результат отправителя
     */
    public void validateResult(AttemptResult result) {
        if (result == null) {
            throw new DeliveryException(
                    ApplicationErrorCode.SENDER_RESULT_MISSING,
                    "Отправитель не вернул результат");
        }
    }

    /**
     * Проверяет, что сценарий отправителя ещё содержит результаты.
     * @param results оставшиеся результаты отправок
     */
    public void validateRemaining(Collection<AttemptResult> results) {
        if (results.isEmpty()) {
            throw new DeliveryException(
                    ApplicationErrorCode.SENDER_RESULTS_EXHAUSTED,
                    "Закончились заданные результаты");
        }
    }
}
