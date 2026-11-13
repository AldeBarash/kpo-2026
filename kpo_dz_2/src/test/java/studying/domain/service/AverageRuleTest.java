package studying.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.MaxScore;
import studying.domain.model.Score;
import studying.domain.model.Total;
import studying.domain.model.Weight;

/** Тесты {@link AverageRule}. */
@DisplayName("Среднее значение (AverageRule)")
class AverageRuleTest {
    private final TotalRule rule = new AverageRule();
    private final Criterion code = new Criterion(CriterionId.next(), "Код",
            new MaxScore(10), new Weight(3));
    private final Criterion tests = new Criterion(CriterionId.next(),
            "Тесты", new MaxScore(5), new Weight(1));
    @DisplayName("считает итог по двум критериям с разным весом")
    @Test
    void calculatesAverage() {
        final Total total = rule.calculate(List.of(code, tests),
                Map.of(code.getId(), new Score(5),
                        tests.getId(), new Score(5)));
        assertEquals(new BigDecimal("62.50"), total.val());
    }
    @DisplayName("считает отсутствующую оценку нулём")
    @Test
    void missingScoreIsZero() {
        final Total total = rule.calculate(List.of(code, tests),
                Map.of(code.getId(), new Score(10)));
        assertEquals(new BigDecimal("75.00"), total.val());
    }
    @DisplayName("даёт 100 при максимальных баллах")
    @Test
    void fullScoresGiveHundred() {
        final Total total = rule.calculate(List.of(code, tests),
                Map.of(code.getId(), new Score(10),
                        tests.getId(), new Score(5)));
        assertEquals(new BigDecimal("100.00"), total.val());
    }
}
