package studying.delivery.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.exception.DeliveryException;
import studying.delivery.model.AttemptResult;
import studying.exception.ApplicationErrorCode;

/** Проверяет согласованность результата попытки и причины ошибки. */
@Component
@RequiredArgsConstructor
public final class AttemptResultValidator {
    private final DeliveryValidation validation;

    /**
     * Проверяет данные перед созданием объекта.
     * @param kind вид результата
     * @param reason причина ошибки
     */
    public void validate(AttemptResult.Kind kind, String reason) {
        validation.required(kind,
                "Вид результата попытки обязателен");
        if (kind == AttemptResult.Kind.SUCCESS && reason != null) {
            throw new DeliveryException(
                    ApplicationErrorCode.INVALID_ATTEMPT_RESULT,
                    "Успех не содержит ошибку");
        }
        if (kind != AttemptResult.Kind.SUCCESS
                && (reason == null || reason.isBlank())) {
            throw new DeliveryException(
                    ApplicationErrorCode.INVALID_ATTEMPT_RESULT,
                    "Причина ошибки обязательна");
        }
    }
}
