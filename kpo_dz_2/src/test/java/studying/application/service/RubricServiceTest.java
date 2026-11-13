package studying.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;
import studying.domain.model.Rubric;
import studying.infrastructure.data.RubricRepositoryImpl;

/** Тесты {@link RubricService}. */
@DisplayName("Сценарии рубрикатора (RubricService)")
class RubricServiceTest {
    private final RubricRepositoryImpl rubrics = new RubricRepositoryImpl();
    private final RubricService service = new RubricService(rubrics);
    @DisplayName("добавляет критерий в рубрикатор")
    @Test
    void addsCriterion() {
        rubrics.save(new Rubric("ПОС"));
        service.addCriterion("Код", 10, 2);
        assertEquals(1, service.listCriteria().size());
    }
    @DisplayName("требует, чтобы рубрикатор был создан")
    @Test
    void requiresRubric() {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> service.addCriterion("Код", 10, 2));
        assertEquals(ApplicationErrorCode.RUBRIC_NOT_FOUND, ex.getCode());
    }
    @DisplayName("не принимает критерий с повторным названием")
    @Test
    void rejectsDuplicate() {
        rubrics.save(new Rubric("ПОС"));
        service.addCriterion("Код", 10, 2);
        final DomainException ex = assertThrows(DomainException.class,
                () -> service.addCriterion("код", 5, 1));
        assertEquals(DomainErrorCode.DUPLICATE_CRITERION, ex.getCode());
    }
}
