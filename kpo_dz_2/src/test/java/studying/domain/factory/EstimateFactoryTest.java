package studying.domain.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
/** Тесты {@link EstimateFactory}. */
@DisplayName("Фабрика оценок (EstimateFactory)")
class EstimateFactoryTest {
    private final EstimateFactory factory = new EstimateFactory();
    private static Criterion criterion() {
        return new Criterion(CriterionId.next(), "Код",
                new MaxScore(10), new Weight(2));
    }
    private static Rubric rubricWith(final Criterion criterion) {
        final Rubric rubric = new Rubric("ДЗ");
        rubric.addCriterion(criterion);
        return rubric;
    }
    @DisplayName("создаёт оценку с допустимым баллом")
    @Test
    void createsValidEstimate() {
        final Criterion criterion = criterion();
        final StudentId student = StudentId.next();
        final Estimate e = factory.create(rubricWith(criterion), student,
                criterion.getId(), new Score(10));
        assertEquals(student, e.studentId());
        assertEquals(criterion.getId(), e.criterionId());
        assertEquals(new Score(10), e.score());
    }
    @DisplayName("отвергает балл выше максимума")
    @Test
    void rejectsScoreAboveMax() {
        final Criterion criterion = criterion();
        final Rubric rubric = rubricWith(criterion);
        final DomainException ex = assertThrows(DomainException.class,
                () -> factory.create(rubric, StudentId.next(),
                        criterion.getId(), new Score(11)));
        assertEquals(DomainErrorCode.SCORE_OUT_OF_RANGE, ex.getCode());
    }
    @DisplayName("отвергает критерий не из рубрики")
    @Test
    void rejectsUnknownCriterion() {
        final Rubric rubric = rubricWith(criterion());
        final DomainException ex = assertThrows(DomainException.class,
                () -> factory.create(rubric, StudentId.next(),
                        CriterionId.next(), new Score(5)));
        assertEquals(DomainErrorCode.CRITERION_NOT_IN_RUBRIC, ex.getCode());
    }
}
