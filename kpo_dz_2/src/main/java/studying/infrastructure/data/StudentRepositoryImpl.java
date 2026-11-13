package studying.infrastructure.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.repository.StudentRepository;

/** Хранилище студентов в памяти. */
public final class StudentRepositoryImpl implements StudentRepository {
    private final Map<StudentId, Student> storage = new LinkedHashMap<>();

    @Override
    public void save(final Student student) {
        storage.put(student.getId(), student);
    }

    @Override
    public Optional<Student> findById(final StudentId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Student> findAll() {
        return List.copyOf(storage.values());
    }
}
