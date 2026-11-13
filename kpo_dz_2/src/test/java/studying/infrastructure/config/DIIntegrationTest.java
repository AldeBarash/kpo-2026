package studying.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.application.POSFacade;
import studying.application.dto.StudentResult;
import studying.application.port.ResultExporter;
import studying.domain.model.Criterion;
import studying.domain.model.Student;
import studying.domain.service.RatedOnlyRule;
import studying.domain.service.TotalRule;

/** Интеграционные тесты через DI: подмена бинов в тестовой конфигурации. */
@DisplayName("Интеграция через DI (подмена бинов)")
class DIIntegrationTest {
    /** Экспортёр-заглушка: запоминает итоги вместо записи в файл. */
    static class CapturingExporter implements ResultExporter {
        private final List<StudentResult> captured = new ArrayList<>();
        @Override
        public void export(final List<StudentResult> results) {
            captured.addAll(results);
        }
    }
    /** Тестовая конфигурация: другое правило итога и заглушка экспорта. */
    @Configuration
    static class TestConfig extends AppConfig {
        @Override
        @Bean
        public TotalRule totalRule() {
            return new RatedOnlyRule();
        }
        @Override
        @Bean
        public ResultExporter resultExporter() {
            return new CapturingExporter();
        }
    }
    @DisplayName("подменённое правило и экспорт работают без правок кода")
    @Test
    void swapsRuleAndExporter() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             TestConfig.class)) {
            final POSFacade facade = context.getBean(POSFacade.class);
            final Criterion code = facade.addCriterion("Код", 10, 3);
            facade.addCriterion("Тесты", 5, 1);
            final Student anna = facade.addStudent("Анна");
            facade.setEstimate(anna.getId(), code.getId(), 10);
            assertEquals(new BigDecimal("100.00"),
                    facade.calculateTotal(anna.getId()).val());
            facade.exportResults();
            final CapturingExporter exporter = (CapturingExporter)
                    context.getBean(ResultExporter.class);
            assertEquals(1, exporter.captured.size());
        }
    }
    @DisplayName("с правилом по умолчанию пропущенный критерий считается нулём")
    @Test
    void defaultRuleCountsMissingAsZero() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class)) {
            final POSFacade facade = context.getBean(POSFacade.class);
            final Criterion code = facade.addCriterion("Код", 10, 3);
            facade.addCriterion("Тесты", 5, 1);
            final Student anna = facade.addStudent("Анна");
            facade.setEstimate(anna.getId(), code.getId(), 10);
            assertEquals(new BigDecimal("75.00"),
                    facade.calculateTotal(anna.getId()).val());
        }
    }
}
