package studying.web;

/**
 * Единое публичное представление ошибки.
 * @param code машинно-читаемый код
 * @param message безопасное сообщение
 */
public record ApiError(String code, String message) { }
