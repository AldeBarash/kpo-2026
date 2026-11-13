package studying.infrastructure.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import studying.application.dto.StudentResult;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.domain.model.Student;
import studying.domain.model.StudentId;
import studying.domain.model.Total;

/** Тесты {@link CsvResultExporter}. */
@DisplayName("Экспорт в CSV (CsvResultExporter)")
class CsvResultExporterTest {
    private static StudentResult result(final String name,
                                        final String total) {
        return new StudentResult(new Student(StudentId.next(), name),
                new Total(new BigDecimal(total)));
    }
    @DisplayName("пишет заголовок (с BOM для Excel) и строки с итогами")
    @Test
    void writesCsv(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("out.csv");
        new CsvResultExporter(file).export(
                List.of(result("Анна", "92.5")));
        assertEquals(List.of("\uFEFFstudent;total", "Анна;92.50"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }
    @DisplayName("экранирует разделитель в имени")
    @Test
    void escapesSeparator(@TempDir final Path dir) throws IOException {
        final Path file = dir.resolve("out.csv");
        new CsvResultExporter(file).export(
                List.of(result("Иванов; Иван", "50")));
        assertEquals("\"Иванов; Иван\";50.00",
                Files.readAllLines(file, StandardCharsets.UTF_8).get(1));
    }
    @DisplayName("сообщает об ошибке записи кодом EXPORT_FAILED")
    @Test
    void failsOnUnwritablePath(@TempDir final Path dir) {
        final ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> new CsvResultExporter(dir).export(List.of()));
        assertEquals(ApplicationErrorCode.EXPORT_FAILED, ex.getCode());
    }
}
