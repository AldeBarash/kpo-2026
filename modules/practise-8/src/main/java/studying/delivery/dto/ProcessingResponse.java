package studying.delivery.dto;

/**
 * Результат синхронной обработки очереди.
 * @param observerFailuresCount число сбоев наблюдателей
 */
public record ProcessingResponse(int observerFailuresCount) { }
