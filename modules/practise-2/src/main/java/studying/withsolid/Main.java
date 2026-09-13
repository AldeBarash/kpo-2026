package studying.withsolid;

import studying.withsolid.model.Report;
import studying.withsolid.service.ReportService;
import studying.withsolid.service.impl.EmailReportSender;
import studying.withsolid.service.impl.JsonReportSaver;
import studying.withsolid.service.impl.SoapReportSaver;
import studying.withsolid.service.impl.TextReportSaver;

import java.time.LocalDateTime;

public class Main {
    /**
     * Runs the report creation, persistence, and delivery demonstration.
     */
    public static void main(String[] args) {
        {
            var now = LocalDateTime.now();
            var report = Report.builder()
                    .title("Отчёт")
                    .date(now.toLocalDate())
                    .time(now.toLocalTime())
                    .carsSold(100)
                    .motorcyclesSold(50)
                    .build();

            var reportService = new ReportService(
                    rep -> {
                        new TextReportSaver().save(rep);
                        new JsonReportSaver().save(rep);
                        new SoapReportSaver().save(rep);
                    },
                    new EmailReportSender()
            );

            reportService.process(report, "example@example.com");
        }
    }
}
