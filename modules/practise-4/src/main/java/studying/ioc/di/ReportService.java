package studying.ioc.di;

import java.util.Objects;
import studying.model.Report;
import studying.service.ReportSaver;
import studying.service.ReportSender;

/** Processes reports using dependencies explicitly supplied by the caller. */
public final class ReportService {
    /** Persists reports before delivery. */
    private final ReportSaver saver;
    /** Delivers saved reports. */
    private final ReportSender sender;

    /**
     * Creates a service with explicit, replaceable collaborators.
     *
     * @param reportSaver report persistence dependency
     * @param reportSender report delivery dependency
     */
    public ReportService(final ReportSaver reportSaver,
                         final ReportSender reportSender) {
        this.saver = Objects.requireNonNull(reportSaver,
                "Сохранитель обязателен");
        this.sender = Objects.requireNonNull(reportSender,
                "Отправитель обязателен");
    }

    /**
     * Saves a report and then sends it to the recipient.
     *
     * @param report report to process
     * @param email recipient email address
     */
    public void process(final Report report, final String email) {
        saver.save(report);
        sender.send(report, email);
    }
}
