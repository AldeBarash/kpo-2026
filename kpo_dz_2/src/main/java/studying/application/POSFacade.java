package studying.application;

import java.util.List;
import studying.application.dto.StudentResult;
import studying.application.service.EstimateService;
import studying.application.service.ResultService;
import studying.application.service.RubricService;
import studying.application.service.StudentService;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.model.Total;

/** Фасад: единая точка сценариев ПОС для консоли. */
public class POSFacade {
    private final StudentService studentService;
    private final RubricService rubricService;
    private final EstimateService estimateService;
    private final ResultService resultService;
    /**
     * Создаёт фасад.
     *
     * @param students - сценарии со студентами
     * @param rubric - сценарии с рубрикатором
     * @param estimates - сценарии с оценками
     * @param results - сценарии с итогами
     */
    public POSFacade(final StudentService students,
                     final RubricService rubric,
                     final EstimateService estimates,
                     final ResultService results) {
        this.studentService = students;
        this.rubricService = rubric;
        this.estimateService = estimates;
        this.resultService = results;
    }
    /**
     * Добавляет студента.
     *
     * @param name - имя
     * @return студент
     */
    public Student addStudent(final String name) {
        return studentService.addStudent(name);
    }
    /** @return студенты */
    public List<Student> listStudents() {
        return studentService.listStudents();
    }
    /**
     * Добавляет критерий.
     *
     * @param name - название
     * @param maxScore - максимум
     * @param weight - вес
     * @return - критерий
     */
    public Criterion addCriterion(final String name,
                                  final int maxScore,
                                  final int weight) {
        return rubricService.addCriterion(name, maxScore, weight);
    }
    /** @return критерии */
    public List<Criterion> listCriteria() {
        return rubricService.listCriteria();
    }
    /**
     * Выставляет оценку.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @param score - балл
     * @return оценка
     */
    public Estimate setEstimate(final StudentId studentId,
                                final CriterionId criterionId,
                                final int score) {
        return estimateService.setEstimate(studentId, criterionId, score);
    }
    /**
     * Исправляет оценку.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @param score - новый балл
     * @return оценка
     */
    public Estimate correctEstimate(final StudentId studentId,
                                    final CriterionId criterionId,
                                    final int score) {
        return estimateService.correctEstimate(studentId, criterionId,
                score);
    }
    /**
     * Отменяет оценку.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     */
    public void cancelEstimate(final StudentId studentId,
                               final CriterionId criterionId) {
        estimateService.cancelEstimate(studentId, criterionId);
    }
    /**
     * Возвращает оценки студента.
     *
     * @param studentId - студент
     * @return оценки
     */
    public List<Estimate> listEstimates(final StudentId studentId) {
        return estimateService.listEstimates(studentId);
    }
    /**
     * Считает итог студента.
     *
     * @param studentId - студент
     * @return итог
     */
    public Total calculateTotal(final StudentId studentId) {
        return resultService.calculateTotal(studentId);
    }
    /** @return итоги всех студентов */
    public List<StudentResult> calculateAll() {
        return resultService.calculateAll();
    }
    /** Экспортирует итоги всех студентов. */
    public void exportResults() {
        resultService.exportResults();
    }
}
