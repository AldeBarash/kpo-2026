package interfaces;

/**
 * Сервис техосмотра: решает, принимать ли транспорт в парк.
 */
public interface IServiceCenter {

    /**
     * Проверяет техническое состояние транспорта.
     *
     * @param transport проверяемый транспорт
     * @return {@code true}, если транспорт можно принять в парк
     */
    boolean checkVehicle(ITransport transport);
}
