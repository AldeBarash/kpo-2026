package studying.ioc.di;

import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.service.ReportSaver;
import studying.service.ReportSender;
import studying.service.impl.EmailReportSender;
import studying.service.impl.TextReportSaver;

/** Declares the object graph for the DI variant. */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {
    /**
     * Provides the directory used by the demonstration application.
     *
     * @return reports directory
     */
    @Bean
    Path reportsDirectory() {
        return Path.of("reports");
    }

    /**
     * Provides the production text saver.
     *
     * @param reportsDirectory reports directory
     * @return text report saver
     */
    @Bean
    ReportSaver reportSaver(final Path reportsDirectory) {
        return new TextReportSaver(reportsDirectory);
    }

    /**
     * Provides a network-free sender.
     *
     * @return safe email sender
     */
    @Bean
    EmailReportSender reportSender() {
        return new EmailReportSender();
    }

    /**
     * Builds the service from its contract-level dependencies.
     *
     * @param saver report persistence dependency
     * @param sender report delivery dependency
     * @return configured report service
     */
    @Bean
    ReportService reportService(final ReportSaver saver,
                                final ReportSender sender) {
        return new ReportService(saver, sender);
    }
}
