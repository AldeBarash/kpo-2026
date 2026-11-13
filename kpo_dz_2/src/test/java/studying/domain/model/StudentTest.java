package studying.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Тесты {@link Student}. */
@DisplayName("Студент (Student)")
class StudentTest {
    @DisplayName("обрезает пробелы в имени")
    @Test
    void trimsName() {
        final Student s = new Student(StudentId.next(), "  Анна  ");
        assertEquals("Анна", s.getName());
    }
    @DisplayName("отвергает пустое имя")
    @ParameterizedTest(name = "имя ''{0}''")
    @ValueSource(strings = {"", "   "})
    void rejectsBlankName(final String name) {
        final DomainException ex = assertThrows(DomainException.class,
                () -> new Student(StudentId.next(), name));
        assertEquals(DomainErrorCode.INVALID_NAME, ex.getCode());
    }
    @DisplayName("равенство определяется по id")
    @Test
    void equalById() {
        final StudentId id = StudentId.next();
        assertEquals(new Student(id, "А"), new Student(id, "Б"));
        assertNotEquals(new Student(id, "А"),
                new Student(StudentId.next(), "А"));
    }
}
