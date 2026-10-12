package studying.delivery.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.model.AttemptResult;
import studying.delivery.validation.AttemptResultValidator;

/** Создаёт проверенные результаты попыток отправки. */
@Component
@RequiredArgsConstructor
public final class AttemptResultMapper {
    private final AttemptResultValidator validator;

    /**
     * Проверяет данные и создаёт результат попытки.
     * @param kind вид результата
     * @param reason причина ошибки или null при успехе
     * @return проверенный результат попытки
     */
    public AttemptResult create(AttemptResult.Kind kind, String reason) {
        validator.validate(kind, reason);
        return new AttemptResult(kind, reason);
    }
}
