package studying.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import studying.Main;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.impl.ReportSaverImpl;

/** Tests ReportService with Spring-replaced collaborators. */
@SpringBootTest(classes = Main.class)
@DisplayName("MockBean-тесты ReportService")
class ReportServiceMockBeanTest {
    private static final Report REPORT = new Report("Продажи",
            LocalDate.of(2026, 9, 29), LocalTime.of(10, 15, 30), 12, 7);
    private static final String EMAIL = "student@hse.ru";

    @Autowired
    private ReportService service;

    @MockitoBean
    private ReportSaver saver;

    @MockitoBean
    private ReportSender sender;

    @Test
    @DisplayName("Сервис сохраняет и отправляет отчёт через мок-бины")
    void savesAndSendsThroughMockBeans() {
        service.process(REPORT, EMAIL);

        verify(saver, times(1)).save(REPORT);
        verify(sender, times(1)).send(REPORT, EMAIL);
    }

    @Test
    @DisplayName("Ошибка мок-сохранителя отменяет отправку отчёта")
    void doesNotSendWhenMockSaverFails() {
        doThrow(new ApplicationException(ApplicationErrorCode.FILE_WRITE_ERROR,
                "Не удалось сохранить отчёт")).when(saver).save(REPORT);

        org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> service.process(REPORT, EMAIL));

        verify(sender, never()).send(REPORT, EMAIL);
    }
}
