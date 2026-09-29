package studying.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSaver;

/** Saves reports as text files in an explicitly supplied directory. */
public final class TextReportSaver implements ReportSaver {
    /** Formats report times in generated file names. */
    private static final DateTimeFormatter TIME_FOR_FILE_NAME =
            DateTimeFormatter.ofPattern("HH-mm-ss");
    /** Directory where reports are written. */
    private final Path outputDirectory;

    /**
     * Creates a saver that writes reports below the supplied directory.
     *
     * @param reportOutputDirectory directory for generated reports
     */
    public TextReportSaver(final Path reportOutputDirectory) {
        this.outputDirectory = Objects.requireNonNull(reportOutputDirectory,
                "Каталог отчётов обязателен");
    }

    /**
     * Returns the deterministic file name for a report.
     *
     * @param report report whose name is calculated
     * @return generated report file name
     */
    public static String fileNameFor(final Report report) {
        Objects.requireNonNull(report, "Отчёт обязателен");
        return "report-%s-%s.txt".formatted(report.date(),
                report.time().format(TIME_FOR_FILE_NAME));
    }

    /**
     * Saves the report and retains an I/O failure as the exception cause.
     *
     * @param report report to save
     */
    @Override
    public void save(final Report report) {
        if (report == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Отчёт обязателен");
        }
        final Path reportFile = outputDirectory.resolve(fileNameFor(report));
        try {
            Files.createDirectories(outputDirectory);
            Files.writeString(reportFile, report.toString());
        } catch (IOException exception) {
            throw new ApplicationException(
                    ApplicationErrorCode.FILE_WRITE_ERROR,
                    "Не удалось сохранить отчёт в " + reportFile, exception);
        }
    }
}
