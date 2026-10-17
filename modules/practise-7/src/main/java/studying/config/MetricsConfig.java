package studying.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studying.metrics.ExecutionMetrics;

@Configuration
public class MetricsConfig {
    @Bean
    public ExecutionMetrics executionMetrics() {
        return new ExecutionMetrics.InMemoryExecutionMetrics();
    }
}
