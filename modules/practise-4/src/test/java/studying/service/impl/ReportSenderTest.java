package studying.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.model.Report;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import studying.service.ReportSender;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты отправителя отчётов")
class ReportSenderTest {

    private static final Report REPORT = new Report("Продажи", LocalDate.of(2026, 9, 29), LocalTime.NOON, 10, 5);
    private static final String EMAIL = "test@hse.ru";

    static class FakeReportSender implements ReportSender {
        private final List<Report> reports = new ArrayList<>();
        private final List<String> emails = new ArrayList<>();

        public List<Report> getReports() { return reports; }
        public List<String> getEmails() { return emails; }

        @Override
        public void send(Report report, String email) {
            reports.add(report);
            emails.add(email);
        }
    }

    @Test
    @DisplayName("Отправитель запоминает доставленный отчёт и адресата")
    void remembersDeliveredReportAndRecipient() {
        FakeReportSender sender = new FakeReportSender();

        sender.send(REPORT, EMAIL);

        assertEquals(1, sender.getReports().size());
        assertEquals(REPORT, sender.getReports().get(0));
        assertEquals(EMAIL, sender.getEmails().get(0));
    }
}
