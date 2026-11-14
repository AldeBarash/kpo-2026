package studying.telegram.events;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class NotificationInbox {
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    @Transactional
    public void accept(CreditsCharged event) {
        int inserted = jdbc.update("""
                INSERT INTO inbox_events (consumer_name, event_id, outcome)
                VALUES ('telegram-notifications', ?, 'NO_BINDING') ON CONFLICT DO NOTHING
                """, event.eventId());
        if (inserted == 0) { return; }
        int queued = jdbc.update("""
                INSERT INTO telegram_notifications (id, event_id, user_id, chat_id, binding_id, payload)
                SELECT ?, ?, user_id, chat_id, id, ?::jsonb FROM telegram_bindings WHERE user_id = ?
                """, UUID.randomUUID(), event.eventId(),
                json.writeValueAsString(new Message(event.notification())), event.userId());
        if (queued > 0) {
            jdbc.update("UPDATE inbox_events SET outcome = 'QUEUED' "
                    + "WHERE consumer_name = 'telegram-notifications' AND event_id = ?", event.eventId());
        }
    }

    public record Message(String text) { }
}
