package impl;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import interfaces.IInventory;
import lombok.Getter;

/**
 * Базовый класс любого объекта на балансе. Хранит и проверяет
 * инвентарный номер и название, чтобы не дублировать это в наследниках.
 */
@Getter
public abstract class InventoryItem implements IInventory {

    /** Инвентарный номер. */
    private final long inventoryNumber;

    /** Наименование. */
    private final String name;

    /**
     * Создаёт объект учёта.
     *
     * @param number положительный инвентарный номер
     * @param title непустое название
     */
    protected InventoryItem(final long number, final String title) {
        if (number <= 0) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Инвентарный номер должен быть положительным");
        }
        if (title == null || title.isBlank()) {
            throw new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Название не может быть пустым");
        }
        this.inventoryNumber = number;
        this.name = title.trim();
    }
}
