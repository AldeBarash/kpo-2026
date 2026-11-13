package studying.domain.service;

import java.util.List;
import java.util.Map;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Score;
import studying.domain.model.Total;

/**
 * Среднее только по критериям, где оценка уже поставлена.
 * Критерии без оценки в итог не входят.
 */
public final class RatedOnlyRule implements TotalRule {
    private final TotalRule base = new AverageRule();
    @Override
    public Total calculate(final List<Criterion> criteria,
                           final Map<CriterionId, Score> scores) {
        final List<Criterion> rated = criteria.stream()
                .filter(c -> scores.containsKey(c.getId()))
                .toList();
        return base.calculate(rated, scores);
    }
}
