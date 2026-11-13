package studying.application.service;

import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.model.Rubric;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.repository.RubricRepository;
import studying.domain.repository.StudentRepository;

final class Lookups {
    private Lookups() {
    }
    static Rubric rubric(final RubricRepository repository) {
        return repository.find().orElseThrow(() ->
                new ApplicationException(
                        ApplicationErrorCode.RUBRIC_NOT_FOUND,
                        "Рубрикатор не создан"));
    }
    static Student student(final StudentRepository repository,
                           final StudentId id) {
        return repository.findById(id).orElseThrow(() ->
                new ApplicationException(
                        ApplicationErrorCode.STUDENT_NOT_FOUND,
                        "Студент не найден"));
    }
}
