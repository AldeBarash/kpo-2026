package studying.domain.factory;

import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.StudentId;

/** Фабрика оценок: создаёт оценку, проверенную по рубрике. */
public class EstimateFactory {
    /**
     * Создаёт оценку, если критерий есть в рубрике, а балл допустим.
     *
     * @param rubric - рубрика, по которой проверяем
     * @param studentId - студент
     * @param criterionId - критерий
     * @param score - балл
     * @return корректная оценка
     */
    public Estimate create(final Rubric rubric,
                           final StudentId studentId,
                           final CriterionId criterionId,
                           final Score score) {
        rubric.checkScore(criterionId, score);
        return new Estimate(studentId, criterionId, score);
    }
}
