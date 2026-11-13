package studying.infrastructure.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.model.Student;
import studying.domain.model.StudentId;

/** Тесты {@link StudentRepositoryImpl}. */
@DisplayName("Хранилище студентов (StudentRepositoryImpl)")
class StudentRepositoryImplTest {
    @DisplayName("находит сохранённого студента по id")
    @Test
    void savesAndFindsById() {
        final StudentRepositoryImpl repo = new StudentRepositoryImpl();
        final Student student = new Student(StudentId.next(), "Анна");
        repo.save(student);
        assertEquals(student, repo.findById(student.getId()).orElseThrow());
    }
    @DisplayName("возвращает пусто для неизвестного id")
    @Test
    void unknownIdIsEmpty() {
        final StudentRepositoryImpl repo = new StudentRepositoryImpl();
        assertTrue(repo.findById(StudentId.next()).isEmpty());
    }
    @DisplayName("отдаёт студентов в порядке добавления")
    @Test
    void keepsInsertionOrder() {
        final StudentRepositoryImpl repo = new StudentRepositoryImpl();
        final Student first = new Student(StudentId.next(), "Анна");
        final Student second = new Student(StudentId.next(), "Борис");
        repo.save(first);
        repo.save(second);
        assertEquals(List.of(first, second), repo.findAll());
    }
}
