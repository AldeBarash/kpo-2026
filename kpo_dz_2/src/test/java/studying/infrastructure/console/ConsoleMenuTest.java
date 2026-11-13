package studying.infrastructure.console;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.application.POSFacade;
import studying.application.service.EstimateService;
import studying.application.service.ResultService;
import studying.application.service.RubricService;
import studying.application.service.StudentService;
import studying.domain.factory.EstimateFactory;
import studying.domain.model.Rubric;
import studying.domain.service.AverageRule;
import studying.domain.service.TotalCalculator;
import studying.infrastructure.data.EstimateRepositoryImpl;
import studying.infrastructure.data.RubricRepositoryImpl;
import studying.infrastructure.data.StudentRepositoryImpl;

/** Тесты {@link ConsoleMenu}. */
@DisplayName("Консольное меню (ConsoleMenu)")
class ConsoleMenuTest {
    private static String run(final String... lines) {
        final StudentRepositoryImpl students = new StudentRepositoryImpl();
        final RubricRepositoryImpl rubrics = new RubricRepositoryImpl();
        rubrics.save(new Rubric("ПОС"));
        final EstimateRepositoryImpl estimates =
                new EstimateRepositoryImpl();
        final POSFacade facade = new POSFacade(
                new StudentService(students),
                new RubricService(rubrics),
                new EstimateService(students, rubrics, estimates,
                        new EstimateFactory()),
                new ResultService(students, rubrics, estimates,
                        new TotalCalculator(new AverageRule()),
                        results -> { }));
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final PrintStream out =
                new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new ConsoleMenu(facade,
                new Scanner(String.join("\n", lines) + "\n"), out).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }
    @DisplayName("сообщает о неизвестной команде")
    @Test
    void unknownCommand() {
        assertTrue(run("99", "0").contains("Нет такой команды"));
    }
    @DisplayName("добавляет студента и показывает его в списке")
    @Test
    void addsAndListsStudent() {
        final String output = run("1", "Анна", "2", "0");
        assertTrue(output.contains("Добавлен: Анна"));
        assertTrue(output.contains("1. Анна"));
    }
    @DisplayName("показывает ошибку домена при балле выше максимума")
    @Test
    void showsDomainError() {
        final String output = run("3", "Код", "10", "3",
                "1", "Анна",
                "5", "1", "1", "12",
                "0");
        assertTrue(output.contains("Ошибка: Балл больше максимального"));
    }
    @DisplayName("просит целое число при неверном вводе")
    @Test
    void asksForInteger() {
        final String output = run("3", "Код", "десять", "0");
        assertTrue(output.contains("Нужно ввести целое число"));
    }
    @DisplayName("прогоняет демо-сценарий до экспорта")
    @Test
    void runsDemo() {
        final String output = run("12", "0");
        assertTrue(output.contains("Ожидаемая ошибка"));
        assertTrue(output.contains("Анна: 92.50%"));
        assertTrue(output.contains("Борис: 52.50%"));
        assertTrue(output.contains("Итоги экспортированы"));
    }
}
