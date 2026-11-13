package studying.domain.model;

import java.util.Objects;
import lombok.Getter;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/** Студент: сущность с идентичностью по id. */
@Getter
public final class Student {
    private final StudentId id;
    private final String name;
    /**
     * Создаёт студента.
     *
     * @param studentId - идентификатор
     * @param studentName - непустое имя
     */
    public Student(final StudentId studentId, final String studentName) {
        if (studentName == null || studentName.isBlank()) {
            throw new DomainException(DomainErrorCode.INVALID_NAME,
                    "Имя студента не может быть пустым");
        }
        this.id = Objects.requireNonNull(studentId, "id");
        this.name = studentName.trim();
    }
    @Override
    public boolean equals(final Object other) {
        return other instanceof Student student && id.equals(student.id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
