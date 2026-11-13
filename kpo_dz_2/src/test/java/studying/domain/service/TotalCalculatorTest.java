package studying.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.MaxScore;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.StudentId;
import studying.domain.model.Weight;

/** Тесты {@link TotalCalculator}. */
@DisplayName("Расчёт итога (TotalCalculator)")
class TotalCalculatorTest {
    private final TotalCalculator calculator =
            new TotalCalculator(new AverageRule());
    private static Criterion criterion() {
        return new Criterion(CriterionId.next(), "Код",
                new MaxScore(10), new Weight(1));
    }
    @DisplayName("считает итог по рубрике и оценкам")
    @Test
    void calculatesTotal() {
        final Criterion criterion = criterion();
        final Rubric rubric = new Rubric("ДЗ");
        rubric.addCriterion(criterion);
        final Estimate estimate = new Estimate(StudentId.next(),
                criterion.getId(), new Score(8));
        assertEquals(new BigDecimal("80.00"),
                calculator.calculate(rubric, List.of(estimate)).val());
    }
    @DisplayName("отвергает пустую рубрику")
    @Test
    void rejectsEmptyRubric() {
        final DomainException ex = assertThrows(DomainException.class,
                () -> calculator.calculate(new Rubric("ДЗ"), List.of()));
        assertEquals(DomainErrorCode.EMPTY_RUBRIC, ex.getCode());
    }
    @DisplayName("отвергает оценку по критерию не из рубрики")
    @Test
    void rejectsForeignCriterion() {
        final Rubric rubric = new Rubric("ДЗ");
        rubric.addCriterion(criterion());
        final Estimate foreign = new Estimate(StudentId.next(),
                CriterionId.next(), new Score(5));
        final DomainException ex = assertThrows(DomainException.class,
                () -> calculator.calculate(rubric, List.of(foreign)));
        assertEquals(DomainErrorCode.CRITERION_NOT_IN_RUBRIC, ex.getCode());
    }
}
