package studying.infrastructure.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import studying.application.dto.StudentResult;
import studying.application.exception.ApplicationErrorCode;
import studying.application.exception.ApplicationException;
import studying.application.port.ResultExporter;

/** Экспорт итогов в CSV-файл (разделитель ";", кодировка UTF-8 с BOM). */
public final class CsvResultExporter implements ResultExporter {
    private static final String BOM = "\uFEFF";
    private static final String SEPARATOR = ";";
    private static final String QUOTE = "\"";
    private final Path file;
    /**
     * Создаёт экспортёр.
     *
     * @param target - файл, в который пишем итоги
     */
    public CsvResultExporter(final Path target) {
        this.file = target;
    }
    @Override
    public void export(final List<StudentResult> results) {
        final List<String> lines = new ArrayList<>();
        lines.add(BOM + "student" + SEPARATOR + "total");
        for (final StudentResult result : results) {
            lines.add(escape(result.student().getName()) + SEPARATOR
                    + result.total().val().toPlainString());
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationException(
                    ApplicationErrorCode.EXPORT_FAILED,
                    "Не удалось записать файл: " + file);
        }
    }
    private static String escape(final String value) {
        if (value.contains(SEPARATOR) || value.contains(QUOTE)
                || value.contains("\n")) {
            return QUOTE + value.replace(QUOTE, QUOTE + QUOTE) + QUOTE;
        }
        return value;
    }
}
