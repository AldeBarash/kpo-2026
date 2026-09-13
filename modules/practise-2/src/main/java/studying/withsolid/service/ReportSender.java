package studying.withsolid.service;

import studying.withsolid.model.Report;

/**
 * Контракт доставки отчёта получателю.
 */
@FunctionalInterface
public interface ReportSender {
    /**
     * Отправляет отчёт по указанному email.
     *
     * @param report отчёт для отправки
     * @param email  адрес получателя
     */
    void send(Report report, String email);
}
