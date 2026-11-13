package studying.domain.model;

import java.util.Objects;

/**
 * Оценка студента по одному критерию.
 *
 * @param studentId - студент
 * @param criterionId - критерий
 * @param score - полученный балл
 */
public record Estimate(StudentId studentId,
                       CriterionId criterionId,
                       Score score) {
    /** Проверяет, что все части оценки заданы. */
    public Estimate {
        Objects.requireNonNull(studentId, "studentId");
        Objects.requireNonNull(criterionId, "criterionId");
        Objects.requireNonNull(score, "score");
    }
}
