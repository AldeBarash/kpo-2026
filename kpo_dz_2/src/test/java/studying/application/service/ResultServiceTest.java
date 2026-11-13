package studying.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.application.dto.StudentResult;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.MaxScore;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.model.Weight;
import studying.domain.service.AverageRule;
import studying.domain.service.TotalCalculator;
import studying.infrastructure.data.EstimateRepositoryImpl;
import studying.infrastructure.data.RubricRepositoryImpl;
import studying.infrastructure.data.StudentRepositoryImpl;

/** Тесты {@link ResultService}. */
@DisplayName("Сценарии итогов (ResultService)")
class ResultServiceTest {
    private final StudentRepositoryImpl students =
            new StudentRepositoryImpl();
    private final RubricRepositoryImpl rubrics = new RubricRepositoryImpl();
    private final EstimateRepositoryImpl estimates =
            new EstimateRepositoryImpl();
    private final List<StudentResult> exported = new ArrayList<>();
    private final ResultService service = new ResultService(students,
            rubrics, estimates, new TotalCalculator(new AverageRule()),
            exported::addAll);
    private final Student student = new Student(StudentId.next(), "Анна");
    private final Criterion criterion = new Criterion(CriterionId.next(),
            "Код", new MaxScore(10), new Weight(2));
    ResultServiceTest() {
        students.save(student);
        final Rubric rubric = new Rubric("ПОС");
        rubric.addCriterion(criterion);
        rubrics.save(rubric);
        estimates.save(new Estimate(student.getId(), criterion.getId(),
                new Score(8)));
    }
    @DisplayName("считает итог студента по его оценкам")
    @Test
    void calculatesTotal() {
        assertEquals(new BigDecimal("80.00"),
                service.calculateTotal(student.getId()).val());
    }
    @DisplayName("не считает итог неизвестному студенту")
    @Test
    void rejectsUnknownStudent() {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> service.calculateTotal(StudentId.next()));
        assertEquals(ApplicationErrorCode.STUDENT_NOT_FOUND, ex.getCode());
    }
    @DisplayName("передаёт итоги всех студентов в порт экспорта")
    @Test
    void exportsResults() {
        service.exportResults();
        assertEquals(1, exported.size());
        assertEquals(student, exported.get(0).student());
        assertEquals(new BigDecimal("80.00"),
                exported.get(0).total().val());
    }
}
