package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import impl.BeginnerTransportFilter;
import impl.ServiceCenter;
import interfaces.IInventory;
import interfaces.ITransport;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import main.ConsoleMenu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.StationManager;

@DisplayName("ConsoleMenu: ввод пользователя и вывод")
class ConsoleMenuTest {

    private static final double DELTA = 1e-9;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    private String runMenu(final ConsoleMenu menu, final String... lines) {
        Scanner in = new Scanner(String.join("\n", lines) + "\n");
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        menu.start(in, out);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("с настоящими классами")
    class WithRealClasses {

        private final StationManager manager = new StationManager(
            new ServiceCenter(), new BeginnerTransportFilter());

        private final ConsoleMenu menu = new ConsoleMenu(manager);

        @Test
        @DisplayName("Сценарий: транспорт, вещи, отчёты, инвентарные номера")
        void fullScenario() {
            String output = runMenu(menu,
                "1", "1", "102", "Xiaomi Essential", "8", "95", "45,5",
                "1", "2", "106", "Green City EBike", "6", "85", "60",
                "1", "3", "108", "City Bike", "9", "80",
                "1", "1", "110", "Broken Scooter", "7", "40", "50",
                "2", "1", "501", "SafeRide Helmet XL",
                "2", "2", "602", "SuperCharge Hub", "350",
                "3", "4", "5", "0");

            assertEquals(3, manager.getVehicleCnt());
            assertEquals(2, manager.getItemCnt());
            assertEquals(455.5, manager.totalEnergy(), DELTA);
            assertTrue(output.contains("Принят: Xiaomi Essential"));
            assertTrue(output.contains("Отклонён (техосмотр не пройден)"));
            assertTrue(output.contains("Единиц транспорта: 3"));
            assertTrue(output.contains("Суммарное суточное потребление: "
                + "455.50 кВт·ч"));
            assertTrue(output.contains("- City Bike (простота: 9)"));
            assertTrue(output.contains("№ 501 - SafeRide Helmet XL"));
            assertTrue(output.contains("До свидания!"));
        }

        @Test
        @DisplayName("Повторный номер: ошибка показана, меню продолжает работу")
        void duplicateNumberShowsErrorAndContinues() {
            String output = runMenu(menu,
                "1", "3", "7", "Bike", "9", "90",
                "1", "3", "7", "Other", "9", "90",
                "3", "0");

            assertTrue(output.contains("Ошибка: Инвентарный номер уже занят"));
            assertEquals(1, manager.getVehicleCnt());
            assertTrue(output.contains("До свидания!"));
        }

        @Test
        @DisplayName("Не число: понятная ошибка вместо падения")
        void notANumberIsReported() {
            String output = runMenu(menu,
                "1", "3", "abc", "0");

            assertTrue(output.contains("Ошибка: Нужно ввести число"));
            assertEquals(0, manager.getVehicleCnt());
        }

        @Test
        @DisplayName("Неизвестные пункт меню и тип отклоняются")
        void unknownChoicesAreRejected() {
            String output = runMenu(menu,
                "9",
                "1", "7", "1", "T", "5", "90",
                "0");

            assertTrue(output.contains("Неизвестный пункт меню: 9"));
            assertTrue(output.contains("Неизвестный тип транспорта: 7"));
        }

        @Test
        @DisplayName("Пустой парк: отчёты сообщают, что данных нет")
        void emptyFleetMessages() {
            String output = runMenu(menu, "4", "5", "0");

            assertTrue(output.contains("Подходящей техники нет"));
            assertTrue(output.contains("На балансе пока ничего нет"));
        }

        @Test
        @DisplayName("Конец ввода без команды выхода: программа завершается")
        void endOfInputStopsMenu() {
            String output = runMenu(menu, "3");

            assertTrue(output.contains("Единиц транспорта: 0"));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("с подменённым StationManager")
    class WithMockedManager {

        @Mock
        private StationManager manager;

        @Test
        @DisplayName("Меню передаёт созданный транспорт в StationManager")
        void menuPassesVehicleToManager() {
            when(manager.putOnBalance(any(ITransport.class)))
                .thenReturn(true);

            String output = runMenu(new ConsoleMenu(manager),
                "1", "3", "1", "Bike", "9", "90", "0");

            verify(manager).putOnBalance(any(ITransport.class));
            assertTrue(output.contains("Принят: Bike"));
        }

        @Test
        @DisplayName("Ошибка ввода: StationManager не вызывается")
        void invalidInputNeverReachesManager() {
            runMenu(new ConsoleMenu(manager),
                "1", "3", "1", "Bike", "99", "90", "0");

            verify(manager, never()).putOnBalance(any(ITransport.class));
            verify(manager, never()).putOnBalance(any(IInventory.class));
        }
    }
}
