package studying.gateway.events;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final DeliveryQueue queue;
    private final KafkaTemplate<String, String> kafka;
    private final DeliveryProperties properties;

    @Scheduled(fixedDelayString = "${app.delivery.poll-ms:1000}")
    public void publish() {
        for (var job : queue.claim()) {
            try {
                kafka.send(properties.topic(), job.messageKey(), job.payload()).get(10, TimeUnit.SECONDS);
                queue.sent(job);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                queue.failed(job, "INTERRUPTED", false, 0);
            } catch (Exception exception) {
                queue.failed(job, "KAFKA_SEND_FAILED", false, 0);
            }
        }
    }
}
