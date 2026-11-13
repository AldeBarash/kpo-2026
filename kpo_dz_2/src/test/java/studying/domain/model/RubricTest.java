package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Rubric}. */
@DisplayName("Рубрика (Rubric)")
class RubricTest {
    private static Criterion criterion(final String name) {
        return new Criterion(CriterionId.next(), name,
                new MaxScore(10), new Weight(1));
    }
    @DisplayName("отвергает пустое название")
    @ParameterizedTest(name = "название ''{0}''")
    @ValueSource(strings = {"", "  "})
    void rejectsBlankName(final String name) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Rubric(name));
        assertEquals(DomainErrorCode.INVALID_NAME, ex.getCode());
    }
    @DisplayName("добавляет критерий и находит его по id")
    @Test
    void addsAndFindsCriterion() {
        final Rubric rubric = new Rubric("ДЗ");
        final Criterion c = criterion("Код");
        rubric.addCriterion(c);
        assertEquals(c, rubric.criterion(c.getId()));
        assertEquals(1, rubric.criteria().size());
    }
    @DisplayName("не принимает дубликат названия без учёта регистра")
    @Test
    void rejectsDuplicateName() {
        final Rubric rubric = new Rubric("ДЗ");
        rubric.addCriterion(criterion("Код"));
        final DomainException ex = assertThrows(DomainException.class,
                () -> rubric.addCriterion(criterion("КОД")));
        assertEquals(DomainErrorCode.DUPLICATE_CRITERION, ex.getCode());
    }
    @DisplayName("бросает ошибку, если критерия нет в рубрике")
    @Test
    void failsOnUnknownCriterion() {
        final Rubric rubric = new Rubric("ДЗ");
        final DomainException ex = assertThrows(DomainException.class,
                () -> rubric.criterion(CriterionId.next()));
        assertEquals(DomainErrorCode.CRITERION_NOT_IN_RUBRIC, ex.getCode());
    }
    @DisplayName("не пускает балл выше максимума")
    @Test
    void rejectsScoreAboveMax() {
        final Rubric rubric = new Rubric("ДЗ");
        final Criterion c = criterion("Код");
        rubric.addCriterion(c);
        rubric.checkScore(c.getId(), new Score(10));
        final DomainException ex = assertThrows(DomainException.class,
                () -> rubric.checkScore(c.getId(), new Score(11)));
        assertEquals(DomainErrorCode.SCORE_OUT_OF_RANGE, ex.getCode());
    }
    @DisplayName("возвращаемый список критериев нельзя менять")
    @Test
    void criteriaListIsImmutable() {
        final Rubric rubric = new Rubric("ДЗ");
        assertThrows(UnsupportedOperationException.class,
                () -> rubric.criteria().add(criterion("Код")));
    }
}
