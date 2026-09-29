package studying.service;


import studying.model.Report;

/** Persists a report in a particular storage format. */
@FunctionalInterface
public interface ReportSaver {
    /**
     * Saves the supplied report.
     *
     * @param report report to persist
     */
    void save(Report report);
}
