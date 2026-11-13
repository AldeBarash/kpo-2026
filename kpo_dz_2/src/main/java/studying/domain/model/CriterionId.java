package studying.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Идентификатор критерия.
 *
 * @param val - значение идентификатора
 */
public record CriterionId(UUID val) {
    /** Проверяет, что значение задано. */
    public CriterionId {
        Objects.requireNonNull(val, "val");
    }

    /**
     * Создаёт новый уникальный идентификатор.
     *
     * @return новый идентификатор
     */
    public static CriterionId next() {
        return new CriterionId(UUID.randomUUID());
    }
}
