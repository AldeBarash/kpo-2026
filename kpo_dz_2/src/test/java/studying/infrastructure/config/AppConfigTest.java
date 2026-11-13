package studying.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import studying.application.POSFacade;
import studying.domain.repository.RubricRepository;
import studying.domain.service.AverageRule;
import studying.domain.service.TotalRule;
import studying.infrastructure.console.ConsoleMenu;

/** Тесты {@link AppConfig}. */
@DisplayName("DI-конфигурация (AppConfig)")
class AppConfigTest {
    @DisplayName("собирает весь граф объектов")
    @Test
    void buildsObjectGraph() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class)) {
            assertInstanceOf(POSFacade.class,
                    context.getBean(POSFacade.class));
            assertInstanceOf(ConsoleMenu.class,
                    context.getBean(ConsoleMenu.class));
        }
    }
    @DisplayName("создаёт рубрикатор при старте")
    @Test
    void createsRubricAtStartup() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class)) {
            assertTrue(context.getBean(RubricRepository.class)
                    .find().isPresent());
        }
    }
    @DisplayName("использует AverageRule как правило по умолчанию")
    @Test
    void usesAverageRuleByDefault() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class)) {
            assertInstanceOf(AverageRule.class,
                    context.getBean(TotalRule.class));
        }
    }
}
