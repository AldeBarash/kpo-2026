package studying.application.service;

import java.util.List;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.repository.StudentRepository;

public class StudentService {
    private final StudentRepository students;
    /**
     * Создаёт сервис.
     *
     * @param studentRepository хранилище студентов
     */
    public StudentService(final StudentRepository studentRepository) {
        this.students = studentRepository;
    }
    /**
     * Добавляет студента.
     *
     * @param name - имя студента
     * @return созданный студент
     */
    public Student addStudent(final String name) {
        final Student student = new Student(StudentId.next(), name);
        students.save(student);
        return student;
    }
    /** @return всех студентов */
    public List<Student> listStudents() {
        return students.findAll();
    }
}
