package studying;

import lombok.Setter;
import lombok.Getter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Customer {

    private String fio;
    private Car car;

    public Customer(String fio) {
        this.fio = fio;
    }
}
