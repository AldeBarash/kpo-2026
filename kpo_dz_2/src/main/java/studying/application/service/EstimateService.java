package studying.application.service;

import java.util.List;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.factory.EstimateFactory;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.StudentId;
import studying.domain.repository.EstimateRepository;
import studying.domain.repository.RubricRepository;
import studying.domain.repository.StudentRepository;

public class EstimateService {
    private final StudentRepository students;
    private final RubricRepository rubrics;
    private final EstimateRepository estimates;
    private final EstimateFactory factory;
    /**
     * Создаёт сервис.
     *
     * @param studentRepository - хранилище студентов
     * @param rubricRepository - хранилище рубрикатора
     * @param estimateRepository - хранилище оценок
     * @param estimateFactory - фабрика оценок
     */
    public EstimateService(final StudentRepository studentRepository,
                           final RubricRepository rubricRepository,
                           final EstimateRepository estimateRepository,
                           final EstimateFactory estimateFactory) {
        this.students = studentRepository;
        this.rubrics = rubricRepository;
        this.estimates = estimateRepository;
        this.factory = estimateFactory;
    }
    /**
     * Выставляет оценку (создаёт или заменяет).
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @param score - балл
     * @return сохранённая оценка
     */
    public Estimate setEstimate(final StudentId studentId,
                                final CriterionId criterionId,
                                final int score) {
        Lookups.student(students, studentId);
        final Rubric rubric = Lookups.rubric(rubrics);
        final Estimate estimate = factory.create(rubric, studentId,
                criterionId, new Score(score));
        estimates.save(estimate);
        return estimate;
    }
    /**
     * Исправляет уже выставленную оценку.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @param score - новый балл
     * @return исправленная оценка
     */
    public Estimate correctEstimate(final StudentId studentId,
                                    final CriterionId criterionId,
                                    final int score) {
        if (estimates.find(studentId, criterionId).isEmpty()) {
            throw notFound();
        }
        return setEstimate(studentId, criterionId, score);
    }
    /**
     * Отменяет оценку.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     */
    public void cancelEstimate(final StudentId studentId,
                               final CriterionId criterionId) {
        if (!estimates.delete(studentId, criterionId)) {
            throw notFound();
        }
    }
    /**
     * Возвращает оценки студента.
     *
     * @param studentId - студент
     * @return оценки
     */
    public List<Estimate> listEstimates(final StudentId studentId) {
        Lookups.student(students, studentId);
        return estimates.findByStudent(studentId);
    }
    private ApplicationException notFound() {
        return new ApplicationException(
                ApplicationErrorCode.ESTIMATE_NOT_FOUND,
                "Оценка не найдена");
    }
}
