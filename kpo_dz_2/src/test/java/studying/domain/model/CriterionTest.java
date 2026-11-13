package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Criterion}. */
@DisplayName("Критерий (Criterion)")
class CriterionTest {

    @DisplayName("обрезает пробелы в названии")
    @Test
    void trimsName() {
        final Criterion c = new Criterion(CriterionId.next(), "  Код  ",
                new MaxScore(10), new Weight(2));
        assertEquals("Код", c.getName());
    }
    @DisplayName("отвергает пустое название")
    @ParameterizedTest(name = "название ''{0}''")
    @ValueSource(strings = {"", "   "})
    void rejectsBlankName(final String name) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Criterion(CriterionId.next(), name,
                        new MaxScore(10), new Weight(2)));
        assertEquals(DomainErrorCode.INVALID_NAME, ex.getCode());
    }
    @DisplayName("проверяет балл по максимуму")
    @Test
    void acceptsByMaxScore() {
        final Criterion c = new Criterion(CriterionId.next(), "Код",
                new MaxScore(10), new Weight(2));
        assertTrue(c.accepts(new Score(10)));
        assertFalse(c.accepts(new Score(11)));
    }
    @DisplayName("равенство определяется по id")
    @Test
    void equalById() {
        final CriterionId id = CriterionId.next();
        final Criterion a = new Criterion(id, "A",
                new MaxScore(5), new Weight(1));
        final Criterion b = new Criterion(id, "B",
                new MaxScore(7), new Weight(3));
        assertEquals(a, b);
        assertNotEquals(a, new Criterion(CriterionId.next(), "A",
                new MaxScore(5), new Weight(1)));
    }
}
