package studying.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import studying.Main;
import studying.model.Report;
import studying.service.impl.ReportSaverImpl;
import studying.service.impl.ReportSenderImpl;

/** Integration tests for the complete Spring object graph. */
@SpringBootTest(classes = Main.class)
@DisplayName("Интеграционные тесты ReportService")
class ReportServiceIntegrationTest {
    @TempDir
    private static Path temporaryDirectory;

    @Autowired
    private ReportService service;

    @Autowired
    private ReportSaver saver;

    @Autowired
    private ReportSender sender;

    final Report report = new Report("Продажи", LocalDate.of(2026, 9, 29),
            LocalTime.of(10, 15, 30), 12, 7);

    @DynamicPropertySource
    static void reportStorageDirectory(final DynamicPropertyRegistry registry) {
        registry.add("report.storage.directory",
                () -> temporaryDirectory.toString());
    }

    @Test
    @DisplayName("Полный сценарий сохраняет файл и передаёт отчёт отправителю")
    void processesReportWithRealSpringBeans() throws IOException {
        service.process(report, "student@hse.ru");

        final Path reportFile = temporaryDirectory.resolve(
                "report-2026-09-29-10-15-30.txt");
        assertEquals(report.toString(), Files.readString(reportFile));
        final ReportSenderImpl safeSender = assertInstanceOf(
                ReportSenderImpl.class, sender);
        assertEquals(report, safeSender.getLastDelivery().report());
        assertEquals("student@hse.ru", safeSender.getLastDelivery().email());
    }

    @Test
    @DisplayName("Spring создаёт сервис и разрешает его зависимости")
    void createsReportServiceAndDependencies() {
        assertNotNull(service);
        assertInstanceOf(ReportSaverImpl.class, saver);
        assertInstanceOf(ReportSenderImpl.class, sender);
    }
}
