package interfaces;

/**
 * Объект, который потребляет энергию.
 */
public interface IEnergyConsumer {

    /**
     * Возвращает суточный расход энергии.
     *
     * @return расход в кВт·ч в сутки, не меньше нуля
     */
    double getDailyEnergyConsumption();
}
