package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link MaxScore}. */
@DisplayName("Максимальный балл (MaxScore)")
class MaxScoreTest {
    @DisplayName("принимает положительное значение")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {1, 5, 100})
    void acceptsPositiveValues(final int val) {
        assertEquals(val, new MaxScore(val).val());
    }
    @DisplayName("отвергает значение не больше 0")
    @ParameterizedTest(name = "значение {0}")
    @ValueSource(ints = {0, -1, -100})
    void rejectsNonPositiveValues(final int val) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new MaxScore(val));
        assertEquals(DomainErrorCode.INVALID_MAX_SCORE, ex.getCode());
    }
    @DisplayName("допускает нулевой балл")
    @Test
    void acceptsZeroScore() {
        assertTrue(new MaxScore(10).accepts(Score.ZERO));
    }
    @DisplayName("допускает балл меньше максимума")
    @Test
    void acceptsScoreBelowMax() {
        assertTrue(new MaxScore(10).accepts(new Score(7)));
    }
    @DisplayName("допускает балл, равный максимуму")
    @Test
    void acceptsScoreEqualToMax() {
        assertTrue(new MaxScore(10).accepts(new Score(10)));
    }
    @DisplayName("не допускает балл выше максимума")
    @Test
    void rejectsScoreAboveMax() {
        assertFalse(new MaxScore(10).accepts(new Score(11)));
    }
    @DisplayName("равенство определяется по значению")
    @Test
    void equalByValue() {
        assertEquals(new MaxScore(10), new MaxScore(10));
    }
}
