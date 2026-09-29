package studying.service.impl;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSender;

/** Sends reports to an email recipient. */
public final class ReportSenderImpl implements ReportSender {
    @Override
    public void send(final Report report, final String email) {
        if (report == null || email == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    String.format("Отчет или email: \"%s\" не может быть null.",
                            email)
            );
        }

        System.out.printf("Отправка отчёта «%s» на email: %s%n",
                report.title(), email);
    }
}
