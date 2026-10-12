package interfaces;

/**
 * Объект, который стоит на балансе и имеет инвентарный номер.
 */
public interface IInventory {

    /**
     * Возвращает инвентарный номер.
     *
     * @return положительный инвентарный номер
     */
    long getInventoryNumber();

    /**
     * Возвращает наименование.
     *
     * @return название объекта
     */
    String getName();
}
