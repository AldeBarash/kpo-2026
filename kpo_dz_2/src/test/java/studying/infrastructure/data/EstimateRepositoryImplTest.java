package studying.infrastructure.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Score;
import studying.domain.model.StudentId;

/** Тесты {@link EstimateRepositoryImpl}. */
@DisplayName("Хранилище оценок (EstimateRepositoryImpl)")
class EstimateRepositoryImplTest {
    @DisplayName("сохраняет и находит оценку")
    @Test
    void savesAndFinds() {
        final EstimateRepositoryImpl repo = new EstimateRepositoryImpl();
        final StudentId student = StudentId.next();
        final CriterionId criterion = CriterionId.next();
        final Estimate estimate =
                new Estimate(student, criterion, new Score(5));
        repo.save(estimate);
        assertEquals(Optional.of(estimate), repo.find(student, criterion));
    }
    @DisplayName("заменяет оценку по той же паре студент-критерий")
    @Test
    void replacesExistingEstimate() {
        final EstimateRepositoryImpl repo = new EstimateRepositoryImpl();
        final StudentId student = StudentId.next();
        final CriterionId criterion = CriterionId.next();
        repo.save(new Estimate(student, criterion, new Score(3)));
        repo.save(new Estimate(student, criterion, new Score(8)));
        assertEquals(1, repo.findByStudent(student).size());
        assertEquals(new Score(8),
                repo.find(student, criterion).orElseThrow().score());
    }
    @DisplayName("удаляет оценку и сообщает, была ли она")
    @Test
    void deletesEstimate() {
        final EstimateRepositoryImpl repo = new EstimateRepositoryImpl();
        final StudentId student = StudentId.next();
        final CriterionId criterion = CriterionId.next();
        repo.save(new Estimate(student, criterion, new Score(5)));
        assertTrue(repo.delete(student, criterion));
        assertFalse(repo.delete(student, criterion));
        assertTrue(repo.find(student, criterion).isEmpty());
    }
    @DisplayName("не смешивает оценки разных студентов")
    @Test
    void separatesStudents() {
        final EstimateRepositoryImpl repo = new EstimateRepositoryImpl();
        final StudentId first = StudentId.next();
        final StudentId second = StudentId.next();
        repo.save(new Estimate(first, CriterionId.next(), new Score(1)));
        assertEquals(1, repo.findByStudent(first).size());
        assertTrue(repo.findByStudent(second).isEmpty());
    }
}
