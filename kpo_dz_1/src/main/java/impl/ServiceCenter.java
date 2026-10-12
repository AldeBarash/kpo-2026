package impl;

import interfaces.IServiceCenter;
import interfaces.ITransport;
import org.springframework.stereotype.Component;

/**
 * Сервис техосмотра: принимает транспорт, у которого техническое
 * состояние не ниже порога.
 */
@Component
public final class ServiceCenter implements IServiceCenter {

    /** Минимальное состояние (в процентах) для приёма в парк. */
    public static final int MIN_CONDITION = 70;

    @Override
    public boolean checkVehicle(final ITransport transport) {
        return transport != null
            && transport.getTechnicalCondition() >= MIN_CONDITION;
    }
}
