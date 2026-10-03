package studying.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import studying.model.Report;

/** Unit tests for the filesystem-backed report saver. */
@DisplayName("Unit-тесты сохранителя отчётов")
class TextReportSaverTest {
    @TempDir
    private Path temporaryDirectory;

    private final Report report = new Report("Продажи",
            LocalDate.of(2026, 9, 29), LocalTime.of(10, 15, 30), 12, 7);

    private ReportSaverImpl saver;

    @BeforeEach
    void setUp() {
        saver = new ReportSaverImpl(temporaryDirectory);
    }

    @Test
    @DisplayName("Сохранитель создаёт файл с ожидаемым именем и содержимым")
    void savesReportUnderDeterministicNameWithItsTextContent()
            throws IOException {
        saver.save(report);

        final Path reportFile = temporaryDirectory.resolve(
                "report-2026-09-29-10-15-30.txt");
        assertTrue(Files.exists(reportFile));
        assertEquals(report.toString(), Files.readString(reportFile));
    }
}
