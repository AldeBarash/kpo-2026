package studying.domain.model;

import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/**
 * Максимальный балл критерия.
 *
 * @param val
 */
public record MaxScore(int val) {
    /** Проверяет, что максимум положительный. */
    public MaxScore {
        if (val <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_MAX_SCORE,
                    "Максимальный балл должен быть положительным");
        }
    }
    /**
     * Проверяет, что балл не больше максимум.
     *
     * @param score проверяемый балл
     * @return true, если балл допустим
     */
    public boolean accepts(final Score score) {
        return score.val() <= val;
    }
}


