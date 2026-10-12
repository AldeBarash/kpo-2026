package impl;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import interfaces.IEnergyConsumer;
import lombok.Getter;

/**
 * Зарядная станция (док-станция, зарядный шкаф): стоит на балансе
 * и потребляет энергию.
 */
@Getter
public final class ChargingStation extends InventoryItem
        implements IEnergyConsumer {

    /** Суточный расход энергии, кВт·ч. */
    private final double dailyEnergyConsumption;

    /**
     * Создаёт зарядную станцию.
     *
     * @param number инвентарный номер
     * @param title название
     * @param dailyEnergy суточный расход энергии, кВт·ч
     */
    public ChargingStation(final long number, final String title,
                           final double dailyEnergy) {
        super(number, title);
        if (Double.isNaN(dailyEnergy) || dailyEnergy < 0) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Расход энергии не может быть отрицательным");
        }
        this.dailyEnergyConsumption = dailyEnergy;
    }
}
