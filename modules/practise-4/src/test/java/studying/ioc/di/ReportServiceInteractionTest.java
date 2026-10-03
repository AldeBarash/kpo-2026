package studying.ioc.di;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import studying.service.ReportService;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSaver;
import studying.service.ReportSender;

/** Mock-based interaction tests for the dependency-injected service. */
@Tag("mock")
@DisplayName("Mock-тесты ReportService без Spring")
class ReportServiceInteractionTest {
    private static final Report REPORT = new Report("Продажи",
            LocalDate.of(2026, 9, 29), LocalTime.of(10, 15), 12, 7);
    private static final String EMAIL = "student@hse.ru";

    @Test
    @DisplayName("Сервис сохраняет и отправляет отчёт с теми же аргументами")
    void savesAndSendsExactlyOnceWithOriginalArguments() {
        final ReportSaver saver = mock(ReportSaver.class);
        final ReportSender sender = mock(ReportSender.class);
        final ReportService service = new ReportService(saver, sender);

        service.process(REPORT, EMAIL);

        verify(saver, times(1)).save(REPORT);
        verify(sender, times(1)).send(REPORT, EMAIL);
    }

    @Test
    @DisplayName("При ошибке сохранения сервис не отправляет отчёт")
    void doesNotSendWhenSavingFails() {
        final ReportSaver saver = mock(ReportSaver.class);
        final ReportSender sender = mock(ReportSender.class);
        final ReportService service = new ReportService(saver, sender);
        doThrow(new ApplicationException(ApplicationErrorCode.FILE_WRITE_ERROR,
                "Запись не удалась")).when(saver).save(REPORT);

        assertThrows(ApplicationException.class,
                () -> service.process(REPORT, EMAIL));

        verify(saver).save(REPORT);
        verify(sender, never()).send(REPORT, EMAIL);
    }
}
