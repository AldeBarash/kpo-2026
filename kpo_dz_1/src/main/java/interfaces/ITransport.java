package interfaces;

/**
 * Транспортное средство. Потреблять энергию оно не обязано:
 * для этого есть отдельный интерфейс {@link IEnergyConsumer}.
 */
public interface ITransport extends IInventory {

    /**
     * Возвращает простоту использования для новичка.
     *
     * @return значение от 1 (сложно) до 10 (очень просто)
     */
    int getEase();

    /**
     * Возвращает техническое состояние, которое оценивает техосмотр.
     *
     * @return значение от 0 до 100 процентов
     */
    int getTechnicalCondition();
}
