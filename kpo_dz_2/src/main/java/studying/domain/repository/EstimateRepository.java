package studying.domain.repository;

import java.util.List;
import java.util.Optional;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.StudentId;

/**
 * Порт хранилища оценок. Оценка однозначно определяется парой
 * "студент + критерий".
 */
public interface EstimateRepository {
    /**
     * Сохраняет оценку; прежняя оценка по той же паре заменяется.
     *
     * @param estimate - оценка
     */
    void save(Estimate estimate);
    /**
     * Ищет оценку студента по критерию.
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @return оценка, если есть
     */
    Optional<Estimate> find(StudentId studentId, CriterionId criterionId);
    /**
     * Удаляет оценку (отмена).
     *
     * @param studentId - студент
     * @param criterionId - критерий
     * @return true, если оценка существовала и была удалена
     */
    boolean delete(StudentId studentId, CriterionId criterionId);
    /**
     * Возвращает все оценки студента.
     *
     * @param studentId - студент
     * @return копия списка оценок
     */
    List<Estimate> findByStudent(StudentId studentId);
}
