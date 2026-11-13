package studying.domain.model;

import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/**
 * Балл, полученный студентом по критерию.
 *
 * @param val - значение балла
 */
public record Score(int val) {
    public static final Score ZERO = new Score(0);
    /** Проверяет, что балл не отрицательный. */
    public Score {
        if (val < 0) {
            throw new DomainException(DomainErrorCode.INVALID_SCORE,
                    "Балл не может быть отрицательным");
        }
    }
}


