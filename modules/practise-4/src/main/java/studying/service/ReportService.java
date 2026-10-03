package studying.service;

import lombok.RequiredArgsConstructor;
import studying.model.Report;

/** Processes reports using dependencies explicitly supplied by the caller. */
@RequiredArgsConstructor
public final class ReportService {
    /** Persists processed reports. */
    private final ReportSaver saver;
    /** Delivers processed reports. */
    private final ReportSender sender;

    /**
     * Saves a report and sends it to the recipient.
     *
     * @param report report to process
     * @param email recipient email address
     */
    public void process(final Report report, final String email) {
        saver.save(report);
        sender.send(report, email);
    }
}
