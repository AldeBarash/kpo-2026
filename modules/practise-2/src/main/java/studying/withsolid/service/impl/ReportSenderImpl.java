package studying.withsolid.service.impl;

import studying.withsolid.exception.ApplicationErrorCode;
import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;
import studying.withsolid.service.ReportSender;

public class ReportSenderImpl implements ReportSender {
    @Override
    public void send(Report report, String email){
        if (report == null || email == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    String.format("Отчет или email: \"%s\" не может быть null.", email)
            );
        }


        System.out.printf("Отправка отчёта «%s» на email: %s%n", report.title(), email);
    }
}
