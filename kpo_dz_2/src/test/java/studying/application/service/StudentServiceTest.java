package studying.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;
import studying.infrastructure.data.StudentRepositoryImpl;

/** Тесты {@link StudentService}. */
@DisplayName("Сценарии студентов (StudentService)")
class StudentServiceTest {
    private final StudentService service =
            new StudentService(new StudentRepositoryImpl());
    @DisplayName("добавляет студента и показывает его в списке")
    @Test
    void addsStudent() {
        service.addStudent("Анна");
        assertEquals(1, service.listStudents().size());
    }
    @DisplayName("не добавляет студента с пустым именем")
    @Test
    void rejectsBlankName() {
        final DomainException ex = assertThrows(DomainException.class,
                () -> service.addStudent("  "));
        assertEquals(DomainErrorCode.INVALID_NAME, ex.getCode());
    }
}
