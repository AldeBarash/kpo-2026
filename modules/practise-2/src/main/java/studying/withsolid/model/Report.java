package studying.withsolid.model;

import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Неизменяемые данные отчёта о продажах автомобилей и мотоциклов.
 * Модель хранит только чистое состояние и не зависит от файловой системы или сети.
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
     * Формирует строгое текстовое представление отчёта для последующего сохранения или отправки.
     *
     * @return отформатированный текст отчёта
     */
    @Override
    public String toString() {
        return String.format(
                "Отчёт: %s%n" +
                        "Дата: %s%n" +
                        "Время: %s%n" +
                        "--------------------------------%n" +
                        "Продано автомобилей: %d шт.%n" +
                        "Продано мотоциклов: %d шт.%n" +
                        "--------------------------------%n",
                title, date, time, carsSold, motorcyclesSold
        );
    }
}
