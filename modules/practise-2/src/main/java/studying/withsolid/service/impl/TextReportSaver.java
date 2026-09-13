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
 * Реализация сохранения текстового представления отчёта в файл.
 */
public class TextReportSaver implements ReportSaver {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    @Override
    public void save(Report report) {
        if (report == null) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Отчёт не должен быть пустым");
        }

        // Формируем имя файла из даты и времени
        String formattedTime = report.time().format(TIME_FORMATTER);
        String fileName = String.format("reports/report-%s-%s.txt", report.date(), formattedTime);
        Path path = Path.of(fileName);

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, report.toString());
        } catch (IOException exception) {
            // Ошибка записи файла сохраняет исходную причину (cause)
            throw new ApplicationException(
                    ApplicationErrorCode.FILE_WRITE_ERROR,
                    "Не удалось сохранить отчёт в файл: " + fileName,
                    exception
            );
        }
    }
}
