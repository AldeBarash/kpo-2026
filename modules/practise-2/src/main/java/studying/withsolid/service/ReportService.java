package studying.withsolid.service;

import lombok.RequiredArgsConstructor;
import studying.withsolid.exception.ApplicationErrorCode;
import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;

/**
 * Основной сервис для управления жизненным циклом отчёта.
 */
@RequiredArgsConstructor
public final class ReportService {
    private final ReportSaver reportSaver;
    private final ReportSender reportSender;

    /**
     * Выполняет последовательный сценарий: сначала валидация и сохранение, затем отправка.
     *
     * @param report отчёт для обработки
     * @param email  email получателя
     */
    public void process(Report report, String email) {
        // Общая валидация на уровне бизнес-логики
        if (report == null) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Отчёт отсутствует");
        }
        if (email == null || email.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Email отсутствует или пуст");
        }
        if (report.carsSold() < 0 || report.motorcyclesSold() < 0) {
            throw new ApplicationException(ApplicationErrorCode.VALIDATION_ERROR, "Числовые показатели не могут быть отрицательными");
        }

        // Выполнение шагов сценария
        reportSaver.save(report);
        reportSender.send(report, email);
    }
}
