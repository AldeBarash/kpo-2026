package studying.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

/**
 * Immutable data that describes one sales report.
 * @param title report title
 * @param date report date
 * @param time report time
 * @param carsSold number of cars sold
 * @param motorcyclesSold number of motorcycles sold
 */
@Builder
public record Report(
        String title,
        LocalDate date,
        LocalTime time,
        int carsSold,
        int motorcyclesSold
) {
    /**
     * Validates the report data before it is made available to services.
     */
    public Report {
        if (title == null || title.isBlank()) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Заголовок отчёта обязателен");
        }
        if (carsSold < 0 || motorcyclesSold < 0) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Количество проданного транспорта не может быть "
                            + "отрицательным");
        }
    }

    /**
     * Produces the text-file representation of this report.
     *
     * @return formatted report content
     */
    @Override
    public String toString() {
        return "%s%nДата: %s%nВремя: %s%n--------------------------------%n"
                .formatted(title, date, time.withNano(0))
                + "Продано автомобилей: %d шт.%n"
                + "Продано мотоциклов: %d шт.%n"
                + "--------------------------------%n"
                .formatted(carsSold, motorcyclesSold);
    }
}
