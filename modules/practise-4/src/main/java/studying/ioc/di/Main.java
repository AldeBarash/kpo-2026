package studying.ioc.di;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import studying.model.Report;

@SpringBootApplication
public final class Main {
    /** Number of cars in the demonstration report. */
    private static final int DEMO_CARS_SOLD = 100;
    /** Number of motorcycles in the demonstration report. */
    private static final int DEMO_MOTORCYCLES_SOLD = 50;

    private Main() { }

    /**
     * Runs the dependency-injection example.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        var context = SpringApplication.run(Main.class);
        var service = context.getBean(ReportService.class);

        service.process(new Report("Продажи", LocalDate.now(), LocalTime.now(),
                DEMO_CARS_SOLD, DEMO_MOTORCYCLES_SOLD), "student@hse.ru");
    }
}
