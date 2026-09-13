package studying;

import lombok.Setter;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
public class HseCarFactory {
    private List<Car> cars = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();

    public void saleCar() {
        customers.stream().filter(customer -> Objects.isNull(customer.getCar()))
                .forEach(customer -> {
                    if (!cars.isEmpty()) {
                        customer.setCar(cars.removeFirst());
                    }
                });
        cars.clear();
    }

    private  int carNum;
    public void addCar(int pedSize) {
        cars.add(new Car(++carNum, pedSize));
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void printCars() {
        cars.forEach(System.out::println);
    }
    public void printCustomers() {
        customers.forEach(System.out::println);
    }
}




//stream
//.filter
//.foreach
//cars.foreach
//System.out::println
