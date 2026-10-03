package studying.configuration;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.service.ReportSaver;
import studying.service.ReportSender;
import studying.service.ReportService;
import studying.service.impl.ReportSaverImpl;
import studying.service.impl.ReportSenderImpl;

/** Declares the object graph that Spring injects into the DI variant. */
@Configuration(proxyBeanMethods = false)
public final class ApplicationConfiguration {
    @Bean
    ReportSaver reportSaver(
            @Value("${report.storage.directory:build/reports}")
            final Path reportsDirectory) {
        return new ReportSaverImpl(reportsDirectory);
    }

    @Bean
    ReportSender reportSender() {
        return new ReportSenderImpl();
    }

    @Bean
    ReportService reportService(final ReportSaver saver,
                                final ReportSender sender) {
        return new ReportService(saver, sender);
    }
}
