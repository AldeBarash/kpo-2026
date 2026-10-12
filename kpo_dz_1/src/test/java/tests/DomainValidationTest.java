package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import impl.Bike;
import impl.ChargingStation;
import impl.EBike;
import impl.Helmet;
import impl.Scooter;
import interfaces.IEnergyConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

@DisplayName("Доменная модель: проверки и типы")
class DomainValidationTest {

    private static void assertValidationError(final Executable action) {
        ApplicationException ex =
                assertThrows(ApplicationException.class, action);
        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, ex.getCode());
    }

    @Test
    @DisplayName("Простота вне диапазона 1-10 недопустима")
    void easeMustBeInRange() {
        assertValidationError(
                () -> new Bike(1, "Bike", 0, 90));
        assertValidationError(
                () -> new Bike(1, "Bike", 11, 90));
    }

    @Test
    @DisplayName("Состояние вне диапазона 0-100 недопустимо")
    void conditionMustBeInRange() {
        assertValidationError(
                () -> new Bike(1, "Bike", 5, -1));
        assertValidationError(
                () -> new Bike(1, "Bike", 5, 101));
    }

    @Test
    @DisplayName("Номер должен быть положительным, название непустым")
    void numberAndNameAreRequired() {
        assertValidationError(
                () -> new Helmet(0, "Helmet"));
        assertValidationError(
                () -> new Helmet(1, "  "));
        assertValidationError(
                () -> new Helmet(1, null));
    }

    @Test
    @DisplayName("Отрицательный расход энергии недопустим")
    void energyMustNotBeNegative() {
        assertValidationError(
                () -> new Scooter(1, "S", 5, 90, -1.0));
        assertValidationError(
                () -> new ChargingStation(1, "C", -1.0));
    }

    @Test
    @DisplayName("Потребители энергии: электротранспорт и станция, не Bike")
    void onlyElectricThingsConsumeEnergy() {
        assertInstanceOf(IEnergyConsumer.class,
                new Scooter(1, "S", 5, 90, 1.0));
        assertInstanceOf(IEnergyConsumer.class,
                new EBike(2, "E", 5, 90, 1.0));
        assertInstanceOf(IEnergyConsumer.class,
                new ChargingStation(3, "C", 1.0));
        assertFalse(IEnergyConsumer.class.isInstance(
                new Bike(4, "B", 5, 90)));
        assertFalse(IEnergyConsumer.class.isInstance(
                new Helmet(5, "H")));
    }

    @Test
    @DisplayName("Название обрезается по краям")
    void nameIsTrimmed() {
        assertEquals("Helmet", new Helmet(1, "  Helmet ").getName());
    }
}

