package studying.service;

import lombok.RequiredArgsConstructor;
import studying.model.Report;

/** Coordinates saving and sending a report. */
@RequiredArgsConstructor
public final class ReportServiceFacade {
    /** Delivers the processed report. */
    private final ReportSender reportSender;
    /** Persists the processed report. */
    private final ReportSaver reportSaver;

    /**
     * Saves a report and sends it to the recipient.
     *
     * @param report report to process
     * @param email recipient email address
     */
    public void process(final Report report, final String email) {
        reportSaver.save(report);

        reportSender.send(report, email);
    }
}
