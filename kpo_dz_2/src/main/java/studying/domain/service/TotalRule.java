package studying.domain.service;

import java.util.List;
import java.util.Map;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Score;
import studying.domain.model.Total;

/** Правило подсчёта итога (стратегия). */
public interface TotalRule {
    /**
     * Считает итог.
     *
     * @param criteria - критерии рубрики
     * @param scores - баллы студента по критериям, где они выставлены
     * @return итог
     */
    Total calculate(List<Criterion> criteria,
                    Map<CriterionId, Score> scores);
}
