package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import impl.Bike;
import impl.ChargingStation;
import impl.EBike;
import impl.Helmet;
import impl.Scooter;
import interfaces.IInventory;
import interfaces.IServiceCenter;
import interfaces.ITransport;
import interfaces.ITransportFilter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.StationManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationManager: техосмотр, баланс, отчёты")
class StationManagerTest {

    private static final double DELTA = 1e-9;

    @Mock
    private IServiceCenter serviceCenter;

    @Mock
    private ITransportFilter filter;

    @Mock
    private ITransport vehicle;

    @Mock
    private ITransport otherVehicle;

    private StationManager manager;

    @BeforeEach
    void setUp() {
        manager = new StationManager(serviceCenter, filter);
    }

    @Test
    @DisplayName("Техосмотр не пройден: транспорт не попадает в парк")
    void rejectedVehicleIsNotAdded() {
        when(vehicle.getInventoryNumber()).thenReturn(1L);
        when(serviceCenter.checkVehicle(vehicle)).thenReturn(false);

        boolean accepted = manager.putOnBalance(vehicle);

        assertFalse(accepted);
        assertEquals(0, manager.getVehicleCnt());
        verify(serviceCenter, times(1)).checkVehicle(vehicle);
    }

    @Test
    @DisplayName("Техосмотр пройден: транспорт принят в парк")
    void approvedVehicleIsAdded() {
        when(vehicle.getInventoryNumber()).thenReturn(2L);
        when(serviceCenter.checkVehicle(vehicle)).thenReturn(true);

        boolean accepted = manager.putOnBalance(vehicle);

        assertTrue(accepted);
        assertEquals(List.of(vehicle), manager.getVehicles());
    }

    @Test
    @DisplayName("Фейк вместо мока: техосмотр можно подменить лямбдой")
    void serviceCenterCanBeReplacedByFake() {
        StationManager strict = new StationManager(t -> false, t -> true);

        assertFalse(strict.putOnBalance(new Bike(3, "Bike", 9, 100)));
    }

    @Test
    @DisplayName("Повторный номер транспорта отклоняется до техосмотра")
    void duplicateVehicleNumberIsRejected() {
        when(vehicle.getInventoryNumber()).thenReturn(4L);
        when(otherVehicle.getInventoryNumber()).thenReturn(4L);
        when(serviceCenter.checkVehicle(vehicle)).thenReturn(true);
        manager.putOnBalance(vehicle);

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> manager.putOnBalance(otherVehicle));
        assertEquals(ApplicationErrorCode.DUPLICATE_INVENTORY_NUMBER,
                ex.getCode());
        verify(serviceCenter, never()).checkVehicle(otherVehicle);
    }

    @Test
    @DisplayName("Номер вещи не может совпадать с номером транспорта")
    void itemNumberMustDifferFromVehicleNumber() {
        when(vehicle.getInventoryNumber()).thenReturn(4L);
        when(serviceCenter.checkVehicle(vehicle)).thenReturn(true);
        manager.putOnBalance(vehicle);

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> manager.putOnBalance(new Helmet(4, "Helmet")));
        assertEquals(ApplicationErrorCode.DUPLICATE_INVENTORY_NUMBER,
                ex.getCode());
    }

    @Test
    @DisplayName("Транспорт нельзя поставить на баланс как вещь")
    void vehicleCannotBypassInspectionAsItem() {
        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> manager.putOnBalance((IInventory) vehicle));
        assertEquals(ApplicationErrorCode.INVALID_ITEM_TYPE, ex.getCode());
        assertEquals(0, manager.getItemCnt());
        verify(serviceCenter, never()).checkVehicle(any(ITransport.class));
    }

    @Test
    @DisplayName("Энергия: суммируются транспорт и вещи-потребители")
    void totalEnergyIncludesVehiclesAndItems() {
        when(serviceCenter.checkVehicle(any(ITransport.class)))
                .thenReturn(true);
        manager.putOnBalance(new Scooter(1, "Scooter", 8, 90, 45.5));
        manager.putOnBalance(new EBike(2, "EBike", 7, 90, 60.0));
        manager.putOnBalance(new Bike(3, "Bike", 9, 90));
        manager.putOnBalance(new ChargingStation(10, "Station", 350.0));
        manager.putOnBalance(new Helmet(11, "Helmet"));

        assertEquals(455.5, manager.totalEnergy(), DELTA);
    }

    @Test
    @DisplayName("Велосипед и шлем энергию не потребляют")
    void nonConsumersGiveZeroEnergy() {
        when(serviceCenter.checkVehicle(any(ITransport.class)))
                .thenReturn(true);
        manager.putOnBalance(new Bike(1, "Bike", 9, 90));
        manager.putOnBalance(new Helmet(2, "Helmet"));

        assertEquals(0.0, manager.totalEnergy(), DELTA);
    }

    @Test
    @DisplayName("Новички: менеджер использует подставленное правило")
    void beginnersListUsesInjectedFilter() {
        when(vehicle.getInventoryNumber()).thenReturn(1L);
        when(otherVehicle.getInventoryNumber()).thenReturn(2L);
        when(serviceCenter.checkVehicle(any(ITransport.class)))
                .thenReturn(true);
        when(filter.matches(vehicle)).thenReturn(true);
        when(filter.matches(otherVehicle)).thenReturn(false);
        manager.putOnBalance(vehicle);
        manager.putOnBalance(otherVehicle);

        assertEquals(List.of(vehicle), manager.transportForBeginners());
        verify(filter, times(1)).matches(vehicle);
        verify(filter, times(1)).matches(otherVehicle);
    }

    @Test
    @DisplayName("Инвентарь: сначала транспорт, затем вещи, список защищён")
    void allInventoryListsVehiclesThenItems() {
        when(serviceCenter.checkVehicle(any(ITransport.class)))
                .thenReturn(true);
        Helmet helmet = new Helmet(1, "Helmet");
        Bike bike = new Bike(2, "Bike", 9, 90);
        manager.putOnBalance(helmet);
        manager.putOnBalance(bike);

        assertEquals(List.of(bike, helmet), manager.getAllInventory());
        assertThrows(UnsupportedOperationException.class,
                () -> manager.getVehicles().clear());
    }

    @Test
    @DisplayName("Отчёт по видам: считает единицы каждого вида")
    void countByTypeGroupsByKind() {
        when(serviceCenter.checkVehicle(any(ITransport.class)))
                .thenReturn(true);
        manager.putOnBalance(new Scooter(1, "S1", 8, 90, 10.0));
        manager.putOnBalance(new Scooter(2, "S2", 8, 90, 10.0));
        manager.putOnBalance(new Helmet(3, "H1"));

        Map<String, Long> counts = manager.countByType();

        assertEquals(2L, counts.get("Scooter").longValue());
        assertEquals(1L, counts.get("Helmet").longValue());
    }

    @Test
    @DisplayName("Пустые значения отклоняются с кодом REQUIRED_VALUE_MISSING")
    void nullArgumentsAreRejected() {
        ApplicationException vehicleEx = assertThrows(
                ApplicationException.class,
                () -> manager.putOnBalance((ITransport) null));
        ApplicationException itemEx = assertThrows(
                ApplicationException.class,
                () -> manager.putOnBalance((IInventory) null));

        assertEquals(ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                vehicleEx.getCode());
        assertEquals(ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                itemEx.getCode());
    }
}
