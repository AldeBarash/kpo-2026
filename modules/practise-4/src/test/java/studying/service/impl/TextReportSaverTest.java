package studying.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import studying.model.Report;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты текстового сохранителя отчётов")
class TextReportSaverTest {

    @Test
    @DisplayName("Файловое сохранение отчёта во временный каталог")
    void savesReportToFileInTempDirectory(@TempDir Path tempDir) throws IOException {
        TextReportSaverImpl saver = new TextReportSaverImpl(tempDir);
        LocalDate date = LocalDate.of(2026, 9, 29);
        Report report = new Report("Квартальный", date, LocalTime.NOON, 100, 50);

        saver.save(report);

        Path expectedFile = tempDir.resolve("report_2026-09-29.txt");
        assertTrue(Files.exists(expectedFile));

        String content = Files.readString(expectedFile);
        assertTrue(content.contains("Title: Квартальный"));
    }
}
