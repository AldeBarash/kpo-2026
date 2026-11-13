package studying.domain.model;

import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/**
 * Вес критерия.
 *
 * @param val - значение критерия
 */
public record Weight(int val) {
    /** Проверяет, что вес положительный. */
    public Weight {
        if (val <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_WEIGHT,
                    "Вес критерия должен быть положительным");
        }
    }
}

