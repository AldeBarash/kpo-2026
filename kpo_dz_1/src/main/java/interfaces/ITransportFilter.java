package interfaces;

/**
 * Правило отбора транспорта (например, «подходит новичку»).
 */
public interface ITransportFilter {

    /**
     * Проверяет, удовлетворяет ли транспорт правилу.
     *
     * @param transport проверяемый транспорт
     * @return {@code true}, если транспорт подходит
     */
    boolean matches(ITransport transport);
}
