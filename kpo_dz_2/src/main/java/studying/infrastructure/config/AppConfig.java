package studying.infrastructure.config;

import java.nio.file.Path;
import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.application.POSFacade;
import studying.application.port.ResultExporter;
import studying.application.service.EstimateService;
import studying.application.service.ResultService;
import studying.application.service.RubricService;
import studying.application.service.StudentService;
import studying.domain.factory.EstimateFactory;
import studying.domain.model.Rubric;
import studying.domain.repository.EstimateRepository;
import studying.domain.repository.RubricRepository;
import studying.domain.repository.StudentRepository;
import studying.domain.service.AverageRule;
import studying.domain.service.TotalCalculator;
import studying.domain.service.TotalRule;
import studying.infrastructure.console.ConsoleMenu;
import studying.infrastructure.data.EstimateRepositoryImpl;
import studying.infrastructure.data.RubricRepositoryImpl;
import studying.infrastructure.data.StudentRepositoryImpl;
import studying.infrastructure.export.CsvResultExporter;

/** Единственное место, где собирается граф объектов. */
@Configuration
public class AppConfig {
    /** @return хранилище студентов */
    @Bean
    public StudentRepository studentRepository() {
        return new StudentRepositoryImpl();
    }
    /** @return хранилище рубрикатора с созданной пустой рубрикой "ПОС" */
    @Bean
    public RubricRepository rubricRepository() {
        final RubricRepository repository = new RubricRepositoryImpl();
        repository.save(new Rubric("ПОС"));
        return repository;
    }
    /** @return хранилище оценок */
    @Bean
    public EstimateRepository estimateRepository() {
        return new EstimateRepositoryImpl();
    }
    /** @return фабрика оценок */
    @Bean
    public EstimateFactory estimateFactory() {
        return new EstimateFactory();
    }
    /** @return правило подсчёта итога (стратегия по умолчанию) */
    @Bean
    public TotalRule totalRule() {
        return new AverageRule();
    }
    /**
     * Создаёт доменный сервис расчёта.
     *
     * @param rule - правило подсчёта
     * @return доменный сервис
     */
    @Bean
    public TotalCalculator totalCalculator(final TotalRule rule) {
        return new TotalCalculator(rule);
    }
    /** @return адаптер экспорта в CSV-файл */
    @Bean
    public ResultExporter resultExporter() {
        return new CsvResultExporter(Path.of("results.csv"));
    }
    /**
     * Создаёт сценарии студентов.
     *
     * @param students - хранилище студентов
     * @return сервис
     */
    @Bean
    public StudentService studentService(final StudentRepository students) {
        return new StudentService(students);
    }
    /**
     * Создаёт сценарии рубрикатора.
     *
     * @param rubrics - хранилище рубрикатора
     * @return сервис
     */
    @Bean
    public RubricService rubricService(final RubricRepository rubrics) {
        return new RubricService(rubrics);
    }
    /**
     * Создаёт сценарии оценок.
     *
     * @param students - хранилище студентов
     * @param rubrics - хранилище рубрикатора
     * @param estimates - хранилище оценок
     * @param factory - фабрика оценок
     * @return сервис
     */
    @Bean
    public EstimateService estimateService(
            final StudentRepository students,
            final RubricRepository rubrics,
            final EstimateRepository estimates,
            final EstimateFactory factory) {
        return new EstimateService(students, rubrics, estimates, factory);
    }

    /**
     * Создаёт сценарии итогов.
     *
     * @param students - хранилище студентов
     * @param rubrics - хранилище рубрикатора
     * @param estimates - хранилище оценок
     * @param calculator - доменный сервис расчёта
     * @param exporter - порт экспорта
     * @return сервис
     */
    @Bean
    public ResultService resultService(
            final StudentRepository students,
            final RubricRepository rubrics,
            final EstimateRepository estimates,
            final TotalCalculator calculator,
            final ResultExporter exporter) {
        return new ResultService(students, rubrics, estimates,
                calculator, exporter);
    }
    /**
     * Создаёт фасад.
     *
     * @param students - сценарии студентов
     * @param rubric - сценарии рубрикатора
     * @param estimates - сценарии оценок
     * @param results - сценарии итогов
     * @return фасад
     */
    @Bean
    public POSFacade posFacade(final StudentService students,
                               final RubricService rubric,
                               final EstimateService estimates,
                               final ResultService results) {
        return new POSFacade(students, rubric, estimates, results);
    }
    /**
     * Создаёт консольное меню.
     *
     * @param facade - фасад
     * @return меню
     */
    @Bean
    public ConsoleMenu consoleMenu(final POSFacade facade) {
        return new ConsoleMenu(facade, new Scanner(System.in), System.out);
    }
}
