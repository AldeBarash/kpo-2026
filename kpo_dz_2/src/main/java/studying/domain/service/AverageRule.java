package studying.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Score;
import studying.domain.model.Total;

/**
 * Среднее по всем критериям рубрики.
 * Критерий без оценки считается равным нулю.
 */
public final class AverageRule implements TotalRule {
    private static final int TEMP_PRECISION = 10;
    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);
    @Override
    public Total calculate(final List<Criterion> criteria,
                           final Map<CriterionId, Score> scores) {
        if (criteria.isEmpty()) {
            return Total.ZERO;
        }
        BigDecimal num = BigDecimal.ZERO;
        int denom = 0;
        for (final Criterion criterion : criteria) {
            final Score score =
                    scores.getOrDefault(criterion.getId(), Score.ZERO);
            final long points =
                    (long) score.val() * criterion.getWeight().val();
            num = num.add(BigDecimal.valueOf(points).divide(
                    BigDecimal.valueOf(criterion.getMaxScore().val()),
                    TEMP_PRECISION, RoundingMode.HALF_UP));
            denom += criterion.getWeight().val();
        }
        return new Total(num.multiply(PERCENT).divide(
                BigDecimal.valueOf(denom),
                Total.PRECISION, RoundingMode.HALF_UP));
    }
}
