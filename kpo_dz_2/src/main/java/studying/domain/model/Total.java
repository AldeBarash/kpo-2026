package studying.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import studying.domain.exception.DomainErrorCode;
import studying.domain.exception.DomainException;

/**
 * Итог по студенту в процентах от 0 до 100.
 *
 * @param val - значение итога
 */
public record Total(BigDecimal val) {
    /** Количество знаков после запятой. */
    public static final int PRECISION = 2;
    private static final BigDecimal MAX = BigDecimal.valueOf(100);
    public static final Total ZERO = new Total(BigDecimal.ZERO);
    /** Проверяет диапазон и приводит значение к двум знакам. */
    public Total {
        Objects.requireNonNull(val, "val");
        if (val.signum() < 0 || val.compareTo(MAX) > 0) {
            throw new DomainException(DomainErrorCode.INVALID_TOTAL,
                    "Итог должен быть в диапазоне от 0 до 100");
        }
        val = val.setScale(PRECISION, RoundingMode.HALF_UP);
    }
}
