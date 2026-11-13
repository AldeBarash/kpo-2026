package studying.application.service;

import java.util.List;
import studying.application.dto.StudentResult;
import studying.application.port.ResultExporter;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.model.Total;
import studying.domain.repository.EstimateRepository;
import studying.domain.repository.RubricRepository;
import studying.domain.repository.StudentRepository;
import studying.domain.service.TotalCalculator;

public class ResultService {
    private final StudentRepository students;
    private final RubricRepository rubrics;
    private final EstimateRepository estimates;
    private final TotalCalculator calculator;
    private final ResultExporter exporter;
    /**
     * Создаёт сервис.
     *
     * @param studentRepository - хранилище студентов
     * @param rubricRepository - хранилище рубрикатора
     * @param estimateRepository - хранилище оценок
     * @param totalCalculator - доменный сервис расчёта итога
     * @param resultExporter - порт экспорта
     */
    public ResultService(final StudentRepository studentRepository,
                         final RubricRepository rubricRepository,
                         final EstimateRepository estimateRepository,
                         final TotalCalculator totalCalculator,
                         final ResultExporter resultExporter) {
        this.students = studentRepository;
        this.rubrics = rubricRepository;
        this.estimates = estimateRepository;
        this.calculator = totalCalculator;
        this.exporter = resultExporter;
    }
    /**
     * Считает итог студента.
     *
     * @param studentId - студент
     * @return итог
     */
    public Total calculateTotal(final StudentId studentId) {
        Lookups.student(students, studentId);
        return calculator.calculate(Lookups.rubric(rubrics),
                estimates.findByStudent(studentId));
    }
    /** @return итоги всех студентов */
    public List<StudentResult> calculateAll() {
        final List<Student> all = students.findAll();
        return all.stream()
                .map(s -> new StudentResult(s, calculateTotal(s.getId())))
                .toList();
    }
    /** Экспортирует итоги всех студентов через порт. */
    public void exportResults() {
        exporter.export(calculateAll());
    }
}
