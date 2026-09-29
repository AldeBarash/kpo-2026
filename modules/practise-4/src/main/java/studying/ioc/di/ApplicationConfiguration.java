package studying.ioc.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.service.ReportSaver;
import studying.service.ReportSender;
import studying.service.impl.ReportSaverImpl;
import studying.service.impl.ReportSenderImpl;

/** Declares the object graph that Spring injects into the DI variant. */
@Configuration(proxyBeanMethods = false)
public final class ApplicationConfiguration {
    @Bean
    ReportSaver reportSaver() {
        return new ReportSaverImpl();
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
