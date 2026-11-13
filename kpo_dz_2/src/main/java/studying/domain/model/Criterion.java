package studying.domain.model;

import java.util.Objects;
import lombok.Getter;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;


@Getter
public final class Criterion {
    private final CriterionId id;
    private final String name;
    private final MaxScore maxScore;
    private final Weight weight;
    /**
     * Создаёт критерий.
     *
     * @param criterionId - идентификатор
     * @param criterionName - непустое название
     * @param max - максимальный балл
     * @param criterionWeight - вес
     */
    public Criterion(final CriterionId criterionId,
                     final String criterionName,
                     final MaxScore max,
                     final Weight criterionWeight) {
        if (criterionName == null || criterionName.isBlank()) {
            throw new DomainException(DomainErrorCode.INVALID_NAME,
                    "Название критерия не может быть пустым");
        }
        this.id = Objects.requireNonNull(criterionId, "id");
        this.name = criterionName.trim();
        this.maxScore = Objects.requireNonNull(max, "maxScore");
        this.weight = Objects.requireNonNull(criterionWeight, "weight");
    }
    /**
     * Проверяет, что балл допустим для этого критерия.
     *
     * @param score - балл
     * @return true, если балл не больше максимума
     */
    public boolean accepts(final Score score) {
        return maxScore.accepts(score);
    }
    @Override
    public boolean equals(final Object other) {
        return other instanceof Criterion criterion
                && id.equals(criterion.id);
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
