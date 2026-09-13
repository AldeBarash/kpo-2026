package studying.withsolid.service;

import lombok.RequiredArgsConstructor;
import studying.withsolid.model.Report;

@RequiredArgsConstructor
public class ReportServiceFacade {
    private final ReportSender reportSender;
    private final ReportSaver reportSaver;
    public void process(Report report, String email) {
        reportSaver.save(report);
        reportSender.send(report, email);
    }
}

