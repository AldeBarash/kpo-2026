package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import impl.BeginnerTransportFilter;
import interfaces.ITransport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeginnerTransportFilter: правило «простота >= 6»")
class BeginnerTransportFilterTest {

    private final BeginnerTransportFilter filter =
        new BeginnerTransportFilter();

    @Mock
    private ITransport transport;

    @ParameterizedTest(name = "простота {0} -> подходит: {1}")
    @CsvSource({"1,false", "5,false", "6,true", "7,true", "10,true"})
    @DisplayName("Граница 6: ниже нельзя, от 6 можно")
    void boundaryIsSix(final int ease, final boolean expected) {
        when(transport.getEase()).thenReturn(ease);

        assertEquals(expected, filter.matches(transport));
    }

    @Test
    @DisplayName("null не подходит")
    void nullDoesNotMatch() {
        assertFalse(filter.matches(null));
    }
}
