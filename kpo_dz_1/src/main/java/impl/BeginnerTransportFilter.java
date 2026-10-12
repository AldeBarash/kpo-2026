package impl;

import interfaces.ITransport;
import interfaces.ITransportFilter;
import org.springframework.stereotype.Component;

/**
 * Правило «подходит новичку»: простота использования не ниже порога.
 */
@Component
public final class BeginnerTransportFilter implements ITransportFilter {

    /** Минимальная простота, при которой выдаём новичкам. */
    public static final int MIN_EASE_FOR_BEGINNER = 6;

    @Override
    public boolean matches(final ITransport transport) {
        return transport != null
            && transport.getEase() >= MIN_EASE_FOR_BEGINNER;
    }
}
