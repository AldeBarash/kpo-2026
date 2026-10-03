package studying.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;

/** Unit tests for the safe in-memory report sender. */
@DisplayName("Unit-тесты отправителя отчётов")
class ReportSenderImplTest {
    private static final Report REPORT = Report.builder()
            .title("Продажи")
            .date(LocalDate.of(2026, 9, 29))
            .carsSold(12)
            .build();

    @Test
    @DisplayName("Отправитель запоминает отправленный отчёт и получателя")
    void remembersDeliveredReportAndRecipient() {
        final ReportSenderImpl sender = new ReportSenderImpl();

        sender.send(REPORT, "student@hse.ru");

        assertEquals(REPORT, sender.getLastDelivery().report());
        assertEquals("student@hse.ru", sender.getLastDelivery().email());
    }

    @Test
    @DisplayName("Отправитель отклоняет отсутствующий email")
    void rejectsMissingEmail() {
        final ReportSenderImpl sender = new ReportSenderImpl();

        final ApplicationException exception = assertThrows(
                ApplicationException.class, () -> sender.send(REPORT, null));

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR,
                exception.getCode());
    }
}
