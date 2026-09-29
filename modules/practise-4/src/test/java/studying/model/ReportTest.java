package studying.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import studying.exception.ApplicationErrorCode;
import studying.exception.ApplicationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 9, 29);
    private static final LocalTime FIXED_TIME = LocalTime.of(12, 0);

    @Test
    void create_CorrectReport_FieldsAreAccessible() {
        // Создаем корректную запись
        Report report = new Report("Успешные продажи", FIXED_DATE, FIXED_TIME, 15, 20);

        // Проверяем поля через методы-акцессоры record (без get)
        assertEquals("Успешные продажи", report.title());
        assertEquals(FIXED_DATE, report.date());
        assertEquals(FIXED_TIME, report.time());
        assertEquals(15, report.carsSold());
        assertEquals(20, report.motorcyclesSold());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", " \n \t "})
    void create_EmptyOrBlankTitle_ThrowsValidationException(String invalidTitle) {
        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                new Report(invalidTitle, FIXED_DATE, FIXED_TIME, 10, 10)
        );
        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, ex.getCode());
    }

    // Программная генерация данных от -3 до 3, как требует задание
    static Stream<Arguments> provideNumbersFromMinusThreeToThree() {
        return IntStream.rangeClosed(-3, 3).mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideNumbersFromMinusThreeToThree")
    void create_ParametricCarValidation_BehavesCorrectly(int carsSold) {
        if (carsSold < 0) {
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                    new Report("Тест", FIXED_DATE, FIXED_TIME, carsSold, 10)
            );
            assertEquals(ApplicationErrorCode.VALIDATION_ERROR, ex.getCode());
        } else {
            assertDoesNotThrow(() -> new Report("Тест", FIXED_DATE, FIXED_TIME, carsSold, 10));
        }
    }

    @ParameterizedTest
    @MethodSource("provideNumbersFromMinusThreeToThree")
    void create_ParametricMotorcycleValidation_BehavesCorrectly(int motorcyclesSold) {
        if (motorcyclesSold < 0) {
            ApplicationException ex = assertThrows(ApplicationException.class, () ->
                    new Report("Тест", FIXED_DATE, FIXED_TIME, 10, motorcyclesSold)
            );
            assertEquals(ApplicationErrorCode.VALIDATION_ERROR, ex.getCode());
        } else {
            assertDoesNotThrow(() -> new Report("Тест", FIXED_DATE, FIXED_TIME, 10, motorcyclesSold));
        }
    }
}
