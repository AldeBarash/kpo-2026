package studying.service;

import studying.model.Report;

/** Delivers a report through a particular communication channel. */
@FunctionalInterface
public interface ReportSender {
    /**
     * Sends the supplied report to the recipient.
     *
     * @param report report to send
     * @param email recipient email address
     */
    void send(Report report, String email);
}
