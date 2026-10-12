package impl;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import interfaces.IEnergyConsumer;
import lombok.Getter;

/**
 * Электросамокат.
 */
@Getter
public final class Scooter extends Vehicle implements IEnergyConsumer {

    /** Суточный расход энергии, кВт·ч. */
    private final double dailyEnergyConsumption;

    /**
     * Создаёт электросамокат.
     *
     * @param number инвентарный номер
     * @param title название
     * @param ease простота для новичка, от 1 до 10
     * @param condition техническое состояние, от 0 до 100
     * @param dailyEnergy суточный расход энергии, кВт·ч
     */
    public Scooter(final long number, final String title, final int ease,
               final int condition, final double dailyEnergy) {
        super(number, title, ease, condition);
        if (Double.isNaN(dailyEnergy) || dailyEnergy < 0) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Расход энергии не может быть отрицательным");
        }
        this.dailyEnergyConsumption = dailyEnergy;
    }
}
