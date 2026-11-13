package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Total}. */
@DisplayName("Итог (Total)")
class TotalTest {
    @DisplayName("приводит значение к двум знакам")
    @Test
    void normalizesScale() {
        assertEquals(new BigDecimal("50.00"),
                new Total(new BigDecimal("50")).val());
    }
    @DisplayName("принимает границы 0 и 100")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(strings = {"0", "100"})
    void acceptsBounds(final String value) {
        assertEquals(new BigDecimal("50.00"),
                new Total(new BigDecimal("50")).val());
    }
    @DisplayName("отвергает значения вне диапазона")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(strings = {"-0.01", "100.01"})
    void rejectsOutOfRange(final String value) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Total(new BigDecimal(value)));
        assertEquals(DomainErrorCode.INVALID_TOTAL, ex.getCode());
    }
}
