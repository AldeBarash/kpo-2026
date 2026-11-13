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

/** Тесты {@link RatedOnlyRule}. */
@DisplayName("Только оценённые критерии (RatedOnlyRule)")
class RatedOnlyRuleTest {
    private final TotalRule rule = new RatedOnlyRule();
    private final Criterion code = new Criterion(CriterionId.next(), "Код",
            new MaxScore(10), new Weight(3));
    private final Criterion tests = new Criterion(CriterionId.next(),
            "Тесты", new MaxScore(5), new Weight(1));
    @DisplayName("игнорирует критерии без оценки")
    @Test
    void ignoresMissingScores() {
        final Total total = rule.calculate(List.of(code, tests),
                Map.of(code.getId(), new Score(10)));
        assertEquals(new BigDecimal("100.00"), total.val());
    }
    @DisplayName("возвращает ноль, если оценок нет")
    @Test
    void noScoresGiveZero() {
        final Total total = rule.calculate(List.of(code, tests), Map.of());
        assertEquals(Total.ZERO, total);
    }
}
