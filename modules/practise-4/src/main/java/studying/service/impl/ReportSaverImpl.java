package studying.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSaver;

/** Saves reports as text files. */
public final class ReportSaverImpl implements ReportSaver {
    /** Formats report times for file names. */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH-mm-ss");

    @Override
    public void save(final Report report) {
        if (report == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Отчет не может быть null"
            );
        }

        var reportForSave = Path.of("modules/practise-2/reports",
                "report-%s-%s.txt".formatted(
                report.date(),
                report.time().format(DATE_TIME_FORMATTER)
        ));

        try {
            Files.createDirectories(reportForSave.getParent());
            Files.writeString(reportForSave, report.toString());
        } catch (IOException exception) {
            throw new ApplicationException(
                    ApplicationErrorCode.FILE_WRITE_ERROR,
                    "Не удалось сохранить отчёт в " + reportForSave, exception);
        }
    }
}
