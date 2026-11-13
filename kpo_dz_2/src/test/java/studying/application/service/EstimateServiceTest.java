package studying.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;
import studying.domain.factory.EstimateFactory;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.MaxScore;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.model.Weight;
import studying.infrastructure.data.EstimateRepositoryImpl;
import studying.infrastructure.data.RubricRepositoryImpl;
import studying.infrastructure.data.StudentRepositoryImpl;

/** Тесты {@link EstimateService}. */
@DisplayName("Сценарии оценок (EstimateService)")
class EstimateServiceTest {
    private final StudentRepositoryImpl students =
            new StudentRepositoryImpl();
    private final RubricRepositoryImpl rubrics = new RubricRepositoryImpl();
    private final EstimateRepositoryImpl estimates =
            new EstimateRepositoryImpl();
    private final EstimateService service = new EstimateService(students,
            rubrics, estimates, new EstimateFactory());
    private final Student student = new Student(StudentId.next(), "Анна");
    private final Criterion criterion = new Criterion(CriterionId.next(),
            "Код", new MaxScore(10), new Weight(2));
    EstimateServiceTest() {
        students.save(student);
        final Rubric rubric = new Rubric("ПОС");
        rubric.addCriterion(criterion);
        rubrics.save(rubric);
    }
    @DisplayName("выставляет оценку и сохраняет её")
    @Test
    void setsEstimate() {
        service.setEstimate(student.getId(), criterion.getId(), 8);
        assertEquals(new Score(8), estimates
                .find(student.getId(), criterion.getId())
                .orElseThrow().score());
    }
    @DisplayName("не выставляет оценку неизвестному студенту")
    @Test
    void rejectsUnknownStudent() {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> service.setEstimate(StudentId.next(),
                        criterion.getId(), 5));
        assertEquals(ApplicationErrorCode.STUDENT_NOT_FOUND, ex.getCode());
    }
    @DisplayName("не выставляет балл выше максимума")
    @Test
    void rejectsScoreAboveMax() {
        final DomainException ex = assertThrows(DomainException.class,
                () -> service.setEstimate(student.getId(),
                        criterion.getId(), 11));
        assertEquals(DomainErrorCode.SCORE_OUT_OF_RANGE, ex.getCode());
    }
    @DisplayName("исправляет существующую оценку")
    @Test
    void correctsEstimate() {
        service.setEstimate(student.getId(), criterion.getId(), 3);
        service.correctEstimate(student.getId(), criterion.getId(), 9);
        assertEquals(new Score(9), estimates
                .find(student.getId(), criterion.getId())
                .orElseThrow().score());
    }
    @DisplayName("не исправляет оценку, которой нет")
    @Test
    void rejectsCorrectionOfMissingEstimate() {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> service.correctEstimate(student.getId(),
                        criterion.getId(), 9));
        assertEquals(ApplicationErrorCode.ESTIMATE_NOT_FOUND, ex.getCode());
    }
    @DisplayName("отменяет оценку")
    @Test
    void cancelsEstimate() {
        service.setEstimate(student.getId(), criterion.getId(), 5);
        service.cancelEstimate(student.getId(), criterion.getId());
        assertTrue(service.listEstimates(student.getId()).isEmpty());
    }
    @DisplayName("не отменяет оценку, которой нет")
    @Test
    void rejectsCancelOfMissingEstimate() {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> service.cancelEstimate(student.getId(),
                        criterion.getId()));
        assertEquals(ApplicationErrorCode.ESTIMATE_NOT_FOUND, ex.getCode());
    }
}
