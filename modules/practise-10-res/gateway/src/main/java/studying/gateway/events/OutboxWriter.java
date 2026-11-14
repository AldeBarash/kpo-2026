package studying.gateway.events;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxWriter {
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(CreditsCharged event) {
        jdbc.update("""
                INSERT INTO outbox_events
                    (id, aggregate_id, aggregate_type, ai_request_id, event_type, schema_version, payload)
                VALUES (?, ?, 'User', ?, ?, ?, ?::jsonb)
                """, event.eventId(), event.userId(), event.requestId(), event.eventType(),
                event.schemaVersion(), json.writeValueAsString(event));
    }
}
