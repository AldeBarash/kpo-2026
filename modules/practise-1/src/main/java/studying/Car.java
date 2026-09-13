package studying;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Car {

    private Engine engine;
    private int vin;

    public Car(int vin, int pedSize) {
        this.vin = vin;
        engine = new Engine(pedSize);
    }

}
