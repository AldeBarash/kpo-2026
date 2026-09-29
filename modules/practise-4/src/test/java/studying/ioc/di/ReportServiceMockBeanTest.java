package studying.ioc.di;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;
import studying.model.Report;
import studying.service.ReportSaver;
import studying.service.ReportSender;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Main.class)
@DisplayName("Mockito-тесты ReportService")
class ReportServiceMockBeanTest {

    private static final Report REPORT = new Report("Продажи", LocalDate.of(2026, 9, 29), LocalTime.NOON, 10, 5);
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

        InOrder inOrder = inOrder(saver, sender);
        inOrder.verify(saver).save(REPORT);
        inOrder.verify(sender).send(REPORT, EMAIL);
    }

    @Test
    @DisplayName("Ошибка мок-сохранителя отменяет отправку отчёта")
    void doesNotSendWhenMockSaverFails() {
        doThrow(new ApplicationException(ApplicationErrorCode.FILE_WRITE_ERROR, "Не удалось сохранить отчёт"))
                .when(saver).save(REPORT);

        assertThrows(ApplicationException.class, () ->
                service.process(REPORT, EMAIL)
        );

        verifyNoInteractions(sender);
    }
}
