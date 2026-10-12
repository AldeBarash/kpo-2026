package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApplicationException: код и сообщение")
class ApplicationExceptionTest {

    @Test
    @DisplayName("Хранит код и сообщение, это непроверяемое исключение")
    void keepsCodeAndMessage() {
        ApplicationException ex = new ApplicationException(
            ApplicationErrorCode.VALIDATION_ERROR, "Плохие данные");

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, ex.getCode());
        assertEquals("Плохие данные", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
