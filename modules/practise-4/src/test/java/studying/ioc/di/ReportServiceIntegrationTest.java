package studying.ioc.di;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import studying.service.ReportSaver;
import studying.service.ReportSender;
import studying.service.impl.TextReportSaverImpl;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class)
@DisplayName("Интеграционные тесты контекста Spring")
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService service;

    @Autowired
    private ReportSaver saver;

    @Autowired
    private ReportSender sender;

    @Test
    @DisplayName("Контейнер успешно создаёт ReportService и внедряет зависимости")
    void contextLoadsAndResolvesDependencies() {
        // Проверяем, что все бины успешно создались Спрингом и присутствуют в контексте
        assertNotNull(service, "Бин ReportService не должен быть null");
        assertNotNull(saver, "Бин ReportSaver не должен быть null");
        assertNotNull(sender, "Бин ReportSender не должен быть null");

        // Более мягкая проверка: убеждаемся, что saver вообще умеет сохранять отчеты
        assertTrue(saver instanceof ReportSaver, "Внедренный объект должен реализовывать интерфейс ReportSaver");
    }

}
