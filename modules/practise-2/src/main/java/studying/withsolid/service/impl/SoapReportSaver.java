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
 * Дополнительная реализация: сохранение отчёта в XML-конверте формата SOAP.
 */
public class SoapReportSaver implements ReportSaver {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    @Override
    public void save(Report report) {
        if (report == null) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Отчёт не должен быть пустым");
        }

        String fileName = String.format("reports/report-%s-%s.xml", report.date(), report.time().format(TIME_FORMATTER));
        Path path = Path.of(fileName);

        // Формируем структуру SOAP Envelope / XML
        String xmlContent = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<soap:Envelope xmlns:soap=\"http://xmlsoap.org\">\n" +
                        "  <soap:Body>\n" +
                        "    <ReportResponse>\n" +
                        "      <title>%s</title>\n" +
                        "      <date>%s</date>\n" +
                        "      <time>%s</time>\n" +
                        "      <carsSold>%d</carsSold>\n" +
                        "      <motorcyclesSold>%d</motorcyclesSold>\n" +
                        "    </ReportResponse>\n" +
                        "  </soap:Body>\n" +
                        "</soap:Envelope>",
                report.title(), report.date(), report.time(), report.carsSold(), report.motorcyclesSold()
        );

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, xmlContent);
        } catch (IOException exception) {
            throw new ApplicationException(ApplicationErrorCode.FILE_WRITE_ERROR, "Ошибка SOAP XML-экспорта: " + fileName, exception);
        }
    }
}
