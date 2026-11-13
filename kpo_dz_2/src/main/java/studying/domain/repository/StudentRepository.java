package studying.domain.repository;

import java.util.List;
import java.util.Optional;
import studying.domain.model.Student;
import studying.domain.model.StudentId;

/** Порт хранилища студентов. */
public interface StudentRepository {
    /**
     * Сохраняет студента (новый или существующий).
     *
     * @param student - студент
     */
    void save(Student student);
    /**
     * Ищет студента по идентификатору.
     *
     * @param id - идентификатор
     * @return студент, если найден
     */
    Optional<Student> findById(StudentId id);
    /** @return всех студентов в порядке добавления */
    List<Student> findAll();
}
