package studying.withsolid.service.impl;

import studying.withsolid.exception.ApplicationErrorCode;
import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;
import studying.withsolid.service.ReportSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Дополнительная реализация: сохранение отчёта в формате JSON.
 */
public class JsonReportSaver implements ReportSaver {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    @Override
    public void save(Report report) {
        if (report == null) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Отчёт не должен быть пустым");
        }

        String fileName = String.format("reports/report-%s-%s.json", report.date(), report.time().format(TIME_FORMATTER));
        Path path = Path.of(fileName);

        // Вручную формируем JSON-строку, чтобы не тащить внешние библиотеки (Jackson/Gson)
        String jsonContent = String.format(
                "{\n  \"title\": \"%s\",\n  \"date\": \"%s\",\n  \"time\": \"%s\",\n  \"carsSold\": %d,\n  \"motorcyclesSold\": %d\n}",
                report.title(), report.date(), report.time(), report.carsSold(), report.motorcyclesSold()
        );

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, jsonContent);
        } catch (IOException exception) {
            throw new ApplicationException(ApplicationErrorCode.FILE_WRITE_ERROR, "Ошибка JSON-экспорта: " + fileName, exception);
        }
    }
}
