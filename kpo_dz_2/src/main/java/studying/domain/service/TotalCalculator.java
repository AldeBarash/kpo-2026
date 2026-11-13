package studying.domain.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Rubric;
import studying.domain.model.Score;
import studying.domain.model.Total;

/** Доменный сервис: считает итог по рубрике и оценкам студента. */
public class TotalCalculator {
    private final TotalRule rule;
    /**
     * Создаёт сервис с выбранным правилом подсчёта.
     *
     * @param totalRule правило подсчёта
     */
    public TotalCalculator(final TotalRule totalRule) {
        this.rule = Objects.requireNonNull(totalRule, "rule");
    }
    /**
     * Считает итог студента.
     *
     * @param rubric - рубрика
     * @param estimates - оценки одного студента
     * @return итог
     */
    public Total calculate(final Rubric rubric,
                           final Collection<Estimate> estimates) {
        if (rubric.criteria().isEmpty()) {
            throw new DomainException(DomainErrorCode.EMPTY_RUBRIC,
                    "В рубрике нет критериев");
        }
        final Map<CriterionId, Score> scores = new HashMap<>();
        for (final Estimate estimate : estimates) {
            rubric.criterion(estimate.criterionId());
            scores.put(estimate.criterionId(), estimate.score());
        }
        return rule.calculate(rubric.criteria(), scores);
    }
}
