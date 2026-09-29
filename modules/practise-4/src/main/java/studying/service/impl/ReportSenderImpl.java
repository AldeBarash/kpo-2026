package studying.service.impl;

import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSender;

/** Sends reports to an email recipient. */
public final class ReportSenderImpl implements ReportSender {
    /** Last delivery performed by this safe, in-memory sender. */
    private Delivery lastDelivery;

    @Override
    public void send(final Report report, final String email) {
        if (report == null || email == null) {
            throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    String.format("Отчет или email: \"%s\" не может быть null.",
                            email)
            );
        }

        lastDelivery = new Delivery(report, email);
    }

    /**
     * Returns the latest delivery for verification or diagnostics.
     *
     * @return latest delivery, or {@code null} when nothing was sent
     */
    public Delivery getLastDelivery() {
        return lastDelivery;
    }

    /**
     * A safe in-memory record of a delivered report.
     *
     * @param report delivered report
     * @param email recipient email
     */
    public record Delivery(Report report, String email) { }
}
