package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Weight}. */
@DisplayName("Вес (Weight)")
class WeightTest {
    @DisplayName("принимает положительное значение")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {1, 3, 100})
    void acceptsPositiveValues(final int val) {
        assertEquals(val, new Weight(val).val());
    }
    @DisplayName("отвергает значение не больше 0")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {0, -1, -100})
    void rejectsNonPositiveValues(final int val) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Weight(val));
        assertEquals(DomainErrorCode.INVALID_WEIGHT, ex.getCode());
    }
    @DisplayName("равенство определяется по значению")
    @Test
    void equalByValue() {
        assertEquals(new Weight(3), new Weight(3));
    }
}
