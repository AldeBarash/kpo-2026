package impl;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import interfaces.ITransport;
import lombok.Getter;

/**
 * Базовый класс транспорта: простота для новичка и состояние.
 */
@Getter
public abstract class Vehicle extends InventoryItem implements ITransport {

    /** Минимальное значение простоты. */
    public static final int MIN_EASE_OF_USE = 1;

    /** Максимальное значение простоты. */
    public static final int MAX_EASE_OF_USE = 10;

    /** Максимальное значение технического состояния. */
    public static final int MAX_CONDITION = 100;

    /** Простота использования для новичка. */
    private final int ease;

    /** Техническое состояние в процентах. */
    private final int technicalCondition;

    /**
     * Создаёт транспорт.
     *
     * @param number инвентарный номер
     * @param title название
     * @param easeValue простота для новичка, от 1 до 10
     * @param condition техническое состояние, от 0 до 100
     */
    protected Vehicle(final long number, final String title,
                      final int easeValue, final int condition) {
        super(number, title);
        if (easeValue < MIN_EASE_OF_USE || easeValue > MAX_EASE_OF_USE) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Простота для новичка должна быть от " + MIN_EASE_OF_USE
                    + " до " + MAX_EASE_OF_USE);
        }
        if (condition < 0 || condition > MAX_CONDITION) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Техническое состояние должно быть от 0 до "
                    + MAX_CONDITION);
        }
        this.ease = easeValue;
        this.technicalCondition = condition;
    }
}
