package studying.ioc.di;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import studying.model.Report;

/** Runs the dependency-injection example. */
@SpringBootApplication
public final class Main {
    /** Number of cars in the demonstration report. */
    private static final int DEMO_CARS_SOLD = 100;
    /** Number of motorcycles in the demonstration report. */
    private static final int DEMO_MOTORCYCLES_SOLD = 50;

    private Main() { }

    /**
     * Starts the application with a demonstration report.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        final var context = SpringApplication.run(Main.class, args);
        final var service = context.getBean(ReportService.class);
        service.process(new Report("Продажи", LocalDate.now(), LocalTime.now(),
                DEMO_CARS_SOLD, DEMO_MOTORCYCLES_SOLD),
                "student@hse.ru");
    }
}
