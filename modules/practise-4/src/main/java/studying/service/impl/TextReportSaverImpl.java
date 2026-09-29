package studying.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSaver;

public final class TextReportSaverImpl implements ReportSaver {
    private final Path targetDirectory;

    // Конструктор по умолчанию для приложения
    public TextReportSaverImpl() {
        this.targetDirectory = Path.of(".");
    }

    // Конструктор для тестов, куда передается временный каталог
    public TextReportSaverImpl(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public void save(Report report) {
        // ИСПРАВЛЕНО: .date() вместо .getDate()
        String fileName = "report_" + report.date() + ".txt";
        Path filePath = targetDirectory.resolve(fileName);

        // ИСПРАВЛЕНО: обращения без get- (title(), carsSold(), motorcyclesSold())
        String content = String.format("Title: %s\nCars: %d\nMotorcycles: %d",
                report.title(), report.carsSold(), report.motorcyclesSold());

        try {
            Files.writeString(filePath, content);
        } catch (IOException e) {
            // Сохраняем исходную причину (cause), как требует валидация
            throw new ApplicationException(
                    ApplicationErrorCode.FILE_WRITE_ERROR,
                    "Ошибка записи отчета в файл",
                    e
            );
        }
    }
}
