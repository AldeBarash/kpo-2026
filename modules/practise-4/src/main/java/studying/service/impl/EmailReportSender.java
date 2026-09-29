package studying.service.impl;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSender;

/**
 * Safe email sender that records delivery instead of using a real SMTP server.
 */
public final class EmailReportSender implements ReportSender {
    /** Most recently recorded delivery. */
    private Delivery lastDelivery;

    /**
     * Records a delivery and writes a demonstration message to standard output.
     *
     * @param report report to send
     * @param email recipient email address
     */
    @Override
    public void send(final Report report, final String email) {
        if (report == null || email == null || email.isBlank()) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Отчёт и непустой email обязательны");
        }
        lastDelivery = new Delivery(report, email);
        System.out.printf("Отправка отчёта «%s» на email: %s%n", report.title(),
                email);
    }

    /**
     * Returns the latest safe delivery, or {@code null} if no report was sent.
     *
     * @return recorded delivery, if any
     */
    public Delivery getLastDelivery() {
        return lastDelivery;
    }

    /**
     * Data recorded by the safe sender.
     *
     * @param report sent report
     * @param email recipient email address
     */
    public record Delivery(Report report, String email) { }
}
