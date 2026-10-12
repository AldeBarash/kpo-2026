package main;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import impl.Bike;
import impl.ChargingStation;
import impl.EBike;
import impl.Helmet;
import impl.Scooter;
import interfaces.IInventory;
import interfaces.ITransport;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import service.StationManager;

/**
 * Консольный интерфейс: читает команды пользователя и вызывает
 * {@link StationManager}. Бизнес-логики здесь нет, только ввод и вывод.
 */
@Component
@RequiredArgsConstructor
public final class ConsoleMenu implements CommandLineRunner {

    /**
     * Тип транспорта: самокат.
     */
    private static final String SCOOTER = "1";

    /**
     * Тип транспорта: электровелосипед.
     */
    private static final String EBIKE = "2";

    /**
     * Тип транспорта: велосипед.
     */
    private static final String BIKE = "3";

    /**
     * Тип вещи: шлем.
     */
    private static final String HELMET = "1";

    /**
     * Тип вещи: зарядная станция.
     */
    private static final String STATION = "2";

    /**
     * Учёт парка (вся бизнес-логика).
     */
    private final StationManager manager;

    @Override
    public void run(final String... args) {
        start(new Scanner(System.in), System.out);
    }

    /**
     * Запускает цикл меню до команды выхода или конца ввода.
     *
     * @param in  источник команд пользователя
     * @param out куда печатать меню и результаты
     */
    public void start(final Scanner in, final PrintStream out) {
        boolean running = true;
        while (running) {
            printMenu(out);
            if (!in.hasNextLine()) {
                return;
            }
            String choice = in.nextLine().trim();
            try {
                running = handle(choice, in, out);
            } catch (ApplicationException e) {
                out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Выполняет выбранный пункт меню.
     *
     * @param choice введённая команда
     * @param in     источник ввода
     * @param out    вывод
     * @return {@code false}, если нужно выйти из программы
     */
    private boolean handle(final String choice, final Scanner in,
                           final PrintStream out) {
        switch (choice) {
            case "1" -> addVehicle(in, out);
            case "2" -> addItem(in, out);
            case "3" -> printReport(out);
            case "4" -> printForBeginners(out);
            case "5" -> printInventory(out);
            case "0" -> {
                out.println("До свидания!");
                return false;
            }
            default -> out.println("Неизвестный пункт меню: " + choice);
        }
        return true;
    }

    /**
     * Печатает меню.
     *
     * @param out вывод
     */
    private void printMenu(final PrintStream out) {
        out.println();
        out.println("=== ВышКат: учёт парка ===");
        out.println("1 - принять транспорт (с техосмотром)");
        out.println("2 - поставить на баланс вещь");
        out.println("3 - отчёт: количество и энергия");
        out.println("4 - техника для новичков");
        out.println("5 - наименования и инвентарные номера");
        out.println("0 - выход");
        out.print("Выберите пункт: ");
    }

    /**
     * Запрашивает данные транспорта и принимает его в парк.
     *
     * @param in  источник ввода
     * @param out вывод
     */
    private void addVehicle(final Scanner in, final PrintStream out) {
        String type = ask(in, out,
                "Тип (1 - самокат, 2 - электровелосипед, 3 - велосипед)");
        long number = askLong(in, out, "Инвентарный номер");
        String title = ask(in, out, "Название");
        int ease = askInt(in, out, "Простота для новичка (1-10)");
        int condition = askInt(in, out, "Состояние (0-100)");
        ITransport vehicle;
        switch (type) {
            case SCOOTER -> vehicle = new Scooter(number, title, ease,
                    condition, askDouble(in, out, "Расход энергии, кВт·ч/сут"));
            case EBIKE -> vehicle = new EBike(number, title, ease,
                    condition, askDouble(in, out, "Расход энергии, кВт·ч/сут"));
            case BIKE -> vehicle = new Bike(number, title, ease, condition);
            default -> throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Неизвестный тип транспорта: " + type);
        }
        if (manager.putOnBalance(vehicle)) {
            out.println("Принят: " + vehicle.getName());
        } else {
            out.println("Отклонён (техосмотр не пройден): "
                    + vehicle.getName());
        }
    }

    /**
     * Запрашивает данные вещи и ставит её на баланс.
     *
     * @param in  источник ввода
     * @param out вывод
     */
    private void addItem(final Scanner in, final PrintStream out) {
        String type = ask(in, out,
                "Тип (1 - шлем, 2 - зарядная станция)");
        long number = askLong(in, out, "Инвентарный номер");
        String title = ask(in, out, "Название");
        IInventory item;
        switch (type) {
            case HELMET -> item = new Helmet(number, title);
            case STATION -> item = new ChargingStation(number, title,
                    askDouble(in, out, "Расход энергии, кВт·ч/сут"));
            default -> throw new ApplicationException(
                    ApplicationErrorCode.VALIDATION_ERROR,
                    "Неизвестный тип вещи: " + type);
        }
        manager.putOnBalance(item);
        out.println("Поставлено на баланс: " + item.getName());
    }

    /**
     * Печатает отчёт по количеству и суммарной энергии.
     *
     * @param out вывод
     */
    private void printReport(final PrintStream out) {
        out.println("Единиц транспорта: " + manager.getVehicleCnt());
        out.println("Вещей на балансе: " + manager.getItemCnt());
        for (Map.Entry<String, Long> entry
                : manager.countByType().entrySet()) {
            out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        out.printf(Locale.ROOT,
                "Суммарное суточное потребление: %.2f кВт·ч%n",
                manager.totalEnergy());
    }

    /**
     * Печатает прокатную технику, подходящую новичкам.
     *
     * @param out вывод
     */
    private void printForBeginners(final PrintStream out) {
        List<ITransport> list = manager.transportForBeginners();
        if (list.isEmpty()) {
            out.println("Подходящей техники нет");
            return;
        }
        for (ITransport vehicle : list) {
            out.println("- " + vehicle.getName() + " (простота: "
                    + vehicle.getEase() + ")");
        }
    }

    /**
     * Печатает наименования и инвентарные номера всего на балансе.
     *
     * @param out вывод
     */
    private void printInventory(final PrintStream out) {
        List<IInventory> all = manager.getAllInventory();
        if (all.isEmpty()) {
            out.println("На балансе пока ничего нет");
            return;
        }
        for (IInventory item : all) {
            out.println("№ " + item.getInventoryNumber() + " - "
                    + item.getName());
        }
    }

    /**
     * Запрашивает строку.
     *
     * @param in     источник ввода
     * @param out    вывод
     * @param prompt подсказка
     * @return введённая строка без пробелов по краям
     */
    private static String ask(final Scanner in, final PrintStream out,
                              final String prompt) {
        out.print(prompt + ": ");
        if (!in.hasNextLine()) {
            throw new ApplicationException(
                    ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                    "Ввод прерван");
        }
        return in.nextLine().trim();
    }

    /**
     * Запрашивает целое число.
     *
     * @param in     источник ввода
     * @param out    вывод
     * @param prompt подсказка
     * @return введённое число
     */
    private static int askInt(final Scanner in, final PrintStream out,
                              final String prompt) {
        String text = ask(in, out, prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw notNumber(text);
        }
    }

    /**
     * Запрашивает длинное целое число.
     *
     * @param in     источник ввода
     * @param out    вывод
     * @param prompt подсказка
     * @return введённое число
     */
    private static long askLong(final Scanner in, final PrintStream out,
                                final String prompt) {
        String text = ask(in, out, prompt);
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw notNumber(text);
        }
    }

    /**
     * Запрашивает дробное число (допускается запятая).
     *
     * @param in     источник ввода
     * @param out    вывод
     * @param prompt подсказка
     * @return введённое число
     */
    private static double askDouble(final Scanner in, final PrintStream out,
                                    final String prompt) {
        String text = ask(in, out, prompt);
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw notNumber(text);
        }
    }

    /**
     * Создаёт ошибку «введено не число».
     *
     * @param text введённый текст
     * @return исключение приложения
     */
    private static ApplicationException notNumber(final String text) {
        return new ApplicationException(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Нужно ввести число, а введено: " + text);
    }
}
