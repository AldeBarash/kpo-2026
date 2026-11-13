package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Score}. */
@DisplayName("Балл (Score)")
class ScoreTest {
    @DisplayName("принимает неотрицательное значение")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {0, 1, 5, 100})
    void acceptsNonNegativeValues(final int val) {
        assertEquals(val, new Score(val).val());
    }
    @DisplayName("отвергает отрицательное значение с кодом INVALID_SCORE")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    void rejectsNegativeValues(final int val) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Score(val));
        assertEquals(DomainErrorCode.INVALID_SCORE, ex.getCode());
    }
    @DisplayName("константа ZERO равна нулю")
    @Test
    void zeroConstantIsZero() {
        assertEquals(0, Score.ZERO.val());
    }
    @DisplayName("равенство определяется по значению")
    @Test
    void equalByValue() {
        assertEquals(new Score(5), new Score(5));
    }
}
