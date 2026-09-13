package studying.withsolid.service.impl;

import studying.withsolid.exception.ApplicationErrorCode;
import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;
import studying.withsolid.service.ReportSender;

/**
 * Имитация отправки отчёта по email через консольный вывод.
 */
public class EmailReportSender implements ReportSender {
    @Override
    public void send(Report report, String email) {
        if (report == null) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Отчёт не должен быть пустым");
        }
        if (email == null || email.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Email получателя не должен быть пустым");
        }

        System.out.printf("Отправка отчёта '%s' на email: %s%n", report.title(), email);
    }
}
