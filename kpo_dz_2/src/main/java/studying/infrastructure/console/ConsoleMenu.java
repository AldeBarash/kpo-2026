package studying.infrastructure.console;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import studying.application.POSFacade;
import studying.application.dto.StudentResult;
import studying.application.exception.ApplicationException;
import studying.domain.exception.DomainException;
import studying.domain.model.Criterion;
import studying.domain.model.CriterionId;
import studying.domain.model.Estimate;
import studying.domain.model.Student;

public class ConsoleMenu {
    private final POSFacade facade;
    private final Scanner in;
    private final PrintStream out;
    private final DemoScenario demo;
    /**
     * Создаёт меню.
     *
     * @param posFacade - фасад сценариев
     * @param input - ввод
     * @param output - вывод
     */
    public ConsoleMenu(final POSFacade posFacade,
                       final Scanner input,
                       final PrintStream output) {
        this.facade = posFacade;
        this.in = input;
        this.out = output;
        this.demo = new DemoScenario(posFacade, output);
    }
    /** Запускает меню и работает до выбора "Выход" или конца ввода. */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            running = in.hasNextLine() && handle(in.nextLine());
        }
    }
    private void printMenu() {
        out.println();
        out.println("=== ПОС: преподавательская оценка студентов ===");
        for (final MenuCommand command : MenuCommand.values()) {
            out.println(command.ordinal() + ". " + command.getLabel());
        }
        out.print("Выберите пункт: ");
    }
    private boolean handle(final String line) {
        final MenuCommand command;
        try {
            command = MenuCommand.values()[Integer.parseInt(line.trim())];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            out.println("Нет такой команды");
            return true;
        }
        if (command == MenuCommand.EXIT) {
            out.println("До свидания");
            return false;
        }
        try {
            execute(command);
        } catch (NumberFormatException e) {
            out.println("Нужно ввести целое число");
        } catch (DomainException | ApplicationException e) {
            out.println("Ошибка: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        } catch (NoSuchElementException e) {
            out.println("Ввод завершён");
            return false;
        }
        return true;
    }
    private void execute(final MenuCommand command) {
        switch (command) {
            case ADD_STUDENT -> addStudent();
            case LIST_STUDENTS -> listStudents();
            case ADD_CRITERION -> addCriterion();
            case LIST_CRITERIA -> listCriteria();
            case SET_ESTIMATE -> setEstimate();
            case CORRECT_ESTIMATE -> correctEstimate();
            case CANCEL_ESTIMATE -> cancelEstimate();
            case LIST_ESTIMATES -> listEstimates();
            case STUDENT_TOTAL -> studentTotal();
            case ALL_TOTALS -> allTotals();
            case EXPORT -> export();
            case DEMO -> demo.run();
            default -> { }
        }
    }
    private void addStudent() {
        final Student student = facade.addStudent(ask("Имя студента: "));
        out.println("Добавлен: " + student.getName());
    }
    private void listStudents() {
        final List<Student> students = facade.listStudents();
        if (students.isEmpty()) {
            out.println("Студентов пока нет");
        }
        for (int i = 0; i < students.size(); i++) {
            out.println((i + 1) + ". " + students.get(i).getName());
        }
    }
    private void addCriterion() {
        final String name = ask("Название критерия: ");
        final int max = askInt("Максимальный балл: ");
        final int weight = askInt("Вес: ");
        final Criterion criterion = facade.addCriterion(name, max, weight);
        out.println("Добавлен критерий: " + describe(criterion));
    }
    private void listCriteria() {
        final List<Criterion> criteria = facade.listCriteria();
        if (criteria.isEmpty()) {
            out.println("Критериев пока нет");
        }
        for (int i = 0; i < criteria.size(); i++) {
            out.println((i + 1) + ". " + describe(criteria.get(i)));
        }
    }
    private void setEstimate() {
        final Student student = chooseStudent();
        final Criterion criterion = chooseCriterion();
        final int score = askInt("Балл: ");
        facade.setEstimate(student.getId(), criterion.getId(), score);
        out.println("Оценка выставлена");
    }
    private void correctEstimate() {
        final Student student = chooseStudent();
        final Criterion criterion = chooseCriterion();
        final int score = askInt("Новый балл: ");
        facade.correctEstimate(student.getId(), criterion.getId(), score);
        out.println("Оценка исправлена");
    }
    private void cancelEstimate() {
        final Student student = chooseStudent();
        final Criterion criterion = chooseCriterion();
        facade.cancelEstimate(student.getId(), criterion.getId());
        out.println("Оценка отменена");
    }
    private void listEstimates() {
        final Student student = chooseStudent();
        final List<Estimate> estimates =
                facade.listEstimates(student.getId());
        if (estimates.isEmpty()) {
            out.println("Оценок нет");
        }
        final Map<CriterionId, String> names = new HashMap<>();
        for (final Criterion criterion : facade.listCriteria()) {
            names.put(criterion.getId(), criterion.getName());
        }
        for (final Estimate estimate : estimates) {
            out.println(names.get(estimate.criterionId()) + ": "
                    + estimate.score().val());
        }
    }
    private void studentTotal() {
        final Student student = chooseStudent();
        out.println("Итог: "
                + facade.calculateTotal(student.getId())
                .val().toPlainString() + "%");
    }
    private void allTotals() {
        final List<StudentResult> results = facade.calculateAll();
        if (results.isEmpty()) {
            out.println("Студентов пока нет");
        }
        for (final StudentResult result : results) {
            out.println(result.student().getName() + ": "
                    + result.total().val().toPlainString() + "%");
        }
    }
    private void export() {
        facade.exportResults();
        out.println("Итоги экспортированы");
    }
    private Student chooseStudent() {
        return choose("Студенты:", facade.listStudents(), Student::getName);
    }
    private Criterion chooseCriterion() {
        return choose("Критерии:", facade.listCriteria(), this::describe);
    }
    private <T> T choose(final String title,
                         final List<T> items,
                         final Function<T, String> label) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Список пуст: " + title);
        }
        out.println(title);
        for (int i = 0; i < items.size(); i++) {
            out.println((i + 1) + ". " + label.apply(items.get(i)));
        }
        final int number = askInt("Номер: ");
        if (number < 1 || number > items.size()) {
            throw new IllegalArgumentException("Нет такого номера");
        }
        return items.get(number - 1);
    }
    private String describe(final Criterion criterion) {
        return criterion.getName() + " (максимум "
                + criterion.getMaxScore().val() + ", вес "
                + criterion.getWeight().val() + ")";
    }
    private String ask(final String prompt) {
        out.print(prompt);
        return in.nextLine();
    }
    private int askInt(final String prompt) {
        return Integer.parseInt(ask(prompt).trim());
    }
}
