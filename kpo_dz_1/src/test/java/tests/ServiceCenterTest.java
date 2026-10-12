package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import impl.ServiceCenter;
import interfaces.ITransport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCenter: решение техосмотра")
class ServiceCenterTest {

    private final ServiceCenter center = new ServiceCenter();

    @Mock
    private ITransport transport;

    @ParameterizedTest(name = "состояние {0} -> принят: {1}")
    @CsvSource({"0,false", "69,false", "70,true", "100,true"})
    @DisplayName("Порог 70: ниже отказ, от 70 принят")
    void inspectionDependsOnCondition(final int condition,
                                      final boolean expected) {
        when(transport.getTechnicalCondition()).thenReturn(condition);

        assertEquals(expected, center.checkVehicle(transport));
    }

    @Test
    @DisplayName("null не проходит техосмотр")
    void nullIsRejected() {
        assertFalse(center.checkVehicle(null));
    }
}
