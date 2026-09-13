package studying.withsolid.service;

import studying.withsolid.model.Report;

/**
 * Контракт для сохранения отчёта.
 */
@FunctionalInterface
public interface ReportSaver {
    /**
     * Сохраняет переданный отчёт.
     *
     * @param report отчёт для сохранения
     */
    void save(Report report);
}
