package studying.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Идентификатор студента.
 *
 * @param val значение идентификатора
 */
public record StudentId(UUID val) {
    /** Проверяет, что значение задано. */
    public StudentId {
        Objects.requireNonNull(val, "val");
    }
    /**
     * Создаёт новый уникальный идентификатор.
     *
     * @return новый идентификатор
     */
    public static StudentId next() {
        return new StudentId(UUID.randomUUID());
    }
}
