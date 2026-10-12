package exception;

/** Коды ошибок приложения учёта проката. */
public enum ApplicationErrorCode {
    /** Обязательное значение не задано. */
    REQUIRED_VALUE_MISSING,
    /** Данные не прошли проверку (диапазон, пустое название и т. п.). */
    VALIDATION_ERROR,
    /** Инвентарный номер уже занят. */
    DUPLICATE_INVENTORY_NUMBER,
    /** Объект нельзя поставить на баланс этим способом. */
    INVALID_ITEM_TYPE
}
