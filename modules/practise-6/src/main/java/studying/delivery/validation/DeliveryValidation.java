package studying.delivery.validation;

import java.util.Collection;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import studying.delivery.exception.DeliveryException;
import studying.exception.ApplicationErrorCode;

/** Общие проверки обязательных данных доставки. */
@Component
@NoArgsConstructor
public final class DeliveryValidation {
    /**
     * Проверяет наличие обязательного значения.
     * @param value проверяемое значение
     * @param message описание ошибки
     */
    public void required(Object value, String message) {
        if (value == null) {
            throw new DeliveryException(
                    ApplicationErrorCode.REQUIRED_VALUE_MISSING, message);
        }
    }

    /**
     * Проверяет наличие коллекции и отсутствие null среди её элементов.
     * @param values проверяемая коллекция
     * @param missingMessage ошибка отсутствующей коллекции
     * @param elementMessage ошибка отсутствующего элемента
     */
    public void requiredElements(Collection<?> values,
                                 String missingMessage,
                                 String elementMessage) {
        required(values, missingMessage);
        for (Object value : values) {
            required(value, elementMessage);
        }
    }
}
