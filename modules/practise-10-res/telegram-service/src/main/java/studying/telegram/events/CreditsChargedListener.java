package studying.telegram.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class CreditsChargedListener {
    private final JsonMapper json;
    private final NotificationInbox inbox;

    @KafkaListener(topics = "${app.delivery.topic}", groupId = "telegram-notifications")
    public void receive(String payload, Acknowledgment acknowledgment) {
        CreditsCharged event = json.readValue(payload, CreditsCharged.class);
        inbox.accept(event); // The proxy commits PostgreSQL before acknowledgment.
        acknowledgment.acknowledge();
    }
}
