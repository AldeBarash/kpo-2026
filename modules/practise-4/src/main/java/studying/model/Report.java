package studying.model;

import java.time.LocalDate;
import java.time.LocalTime;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

/**
 * Immutable data that describes one sales report.
 *
 * @param title report title
 * @param date report date
 * @param time report time
 * @param carsSold number of cars sold
 * @param motorcyclesSold number of motorcycles sold
 */
public record Report(String title, LocalDate date, LocalTime time,
                     int carsSold, int motorcyclesSold) {
    /** Validates report data at construction time. */
    public Report {
        if (title == null || title.isBlank()) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Заголовок отчёта обязателен");
        }
        if (date == null || time == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Дата и время отчёта обязательны");
        }
        if (carsSold < 0) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Число проданных автомобилей не может быть отрицательным");
        }
        if (motorcyclesSold < 0) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Число проданных мотоциклов не может быть отрицательным");
        }
    }

    /** Produces the text-file representation of this report. */
    @Override
    public String toString() {
        return "%s%nДата: %s%nВремя: %s%n--------------------------------%n"
                .formatted(title, date, time.withNano(0))
                + "Продано автомобилей: %d шт.%n".formatted(carsSold)
                + "Продано мотоциклов: %d шт.%n".formatted(motorcyclesSold)
                + "--------------------------------%n";
    }
}
