package studying.infrastructure.console;

import java.io.PrintStream;
import studying.application.POSFacade;
import studying.application.dto.StudentResult;
import studying.application.exception.ApplicationException;
import studying.domain.exception.DomainException;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Student;

/**
 * Сквозной сценарий: студенты и критерии, оценки, итог, экспорт.
 * Заодно показывает отказы при нарушении правил домена.
 */
public class DemoScenario {
    private static final int CODE_MAX = 10;
    private static final int CODE_WEIGHT = 3;
    private static final int TESTS_MAX = 5;
    private static final int TESTS_WEIGHT = 1;
    private static final int ANNA_CODE = 9;
    private static final int ANNA_TESTS = 5;
    private static final int BORIS_CODE = 6;
    private static final int BORIS_CODE_FIXED = 7;
    private static final int BORIS_TESTS = 2;
    private static final int TOO_HIGH = 12;
    private final POSFacade facade;
    private final PrintStream out;
    /**
     * Создаёт сценарий.
     *
     * @param posFacade - фасад сценариев
     * @param output вывод
     */
    public DemoScenario(final POSFacade posFacade, final PrintStream output) {
        this.facade = posFacade;
        this.out = output;
    }
    /** Прогоняет сценарий. */
    public void run() {
        out.println("--- Демо: критерии и студенты ---");
        final Criterion code = facade.addCriterion("Код",
                CODE_MAX, CODE_WEIGHT);
        final Criterion tests = facade.addCriterion("Тесты",
                TESTS_MAX, TESTS_WEIGHT);
        final Student anna = facade.addStudent("Анна");
        final Student boris = facade.addStudent("Борис");
        out.println("Критерии: Код, Тесты. Студенты: Анна, Борис");

        out.println("--- Демо: оценки ---");
        facade.setEstimate(anna.getId(), code.getId(), ANNA_CODE);
        facade.setEstimate(anna.getId(), tests.getId(), ANNA_TESTS);
        facade.setEstimate(boris.getId(), code.getId(), BORIS_CODE);
        out.println("Оценки Анны и Бориса выставлены");

        out.println("--- Демо: нарушение правил ---");
        out.println("Балл 12 при максимуме 10:");
        expectError(() -> facade.setEstimate(boris.getId(),
                code.getId(), TOO_HIGH));
        out.println("Критерий не из рубрикатора:");
        expectError(() -> facade.setEstimate(boris.getId(),
                CriterionId.next(), 1));

        out.println("--- Демо: исправление и отмена ---");
        facade.correctEstimate(boris.getId(), code.getId(), BORIS_CODE_FIXED);
        facade.setEstimate(boris.getId(), tests.getId(), BORIS_TESTS);
        facade.cancelEstimate(boris.getId(), tests.getId());
        out.println("Оценка Бориса по «Коду» исправлена, по «Тестам» отменена");

        out.println("--- Демо: итоги ---");
        for (final StudentResult result : facade.calculateAll()) {
            out.println(result.student().getName() + ": "
                    + result.total().val().toPlainString() + "%");
        }
        facade.exportResults();
        out.println("Итоги экспортированы");
    }
    private void expectError(final Runnable action) {
        try {
            action.run();
            out.println("Ошибка не возникла (так быть не должно)");
        } catch (DomainException | ApplicationException e) {
            out.println("  Ожидаемая ошибка: " + e.getMessage());
        }
    }
}
