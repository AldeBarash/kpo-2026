package studying.metrics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public interface ExecutionMetrics {
    void recordExecution(String operationName, Duration duration, boolean success);

    // Внутренний класс реализации в памяти для тестов
    class InMemoryExecutionMetrics implements ExecutionMetrics {
        public record MetricEntry(String operationName, Duration duration, boolean success) {}
        private final List<MetricEntry> entries = new ArrayList<>();

        @Override
        synchronized public void recordExecution(String operationName, Duration duration, boolean success) {
            entries.add(new MetricEntry(operationName, duration, success));
        }

        public List<MetricEntry> getEntries() { return entries; }
    }
}
