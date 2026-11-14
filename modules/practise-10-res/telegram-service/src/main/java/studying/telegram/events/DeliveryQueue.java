package studying.telegram.events;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Short PostgreSQL transactions; never keep row locks during network calls. */
@Repository
@RequiredArgsConstructor
public class DeliveryQueue {
    public record Job(UUID id, UUID token, String payload, int attempts,
                      String messageKey, Long chatId, UUID bindingId) { }

    private final JdbcTemplate jdbc;
    private final DeliveryProperties properties;

    @Transactional
    public List<Job> claim() {
        // One job per claim: no batch can outlive its lease waiting behind another send.
        String extra = "NULL::text AS message_key, chat_id, binding_id";
        return jdbc.query("""
                WITH candidate AS (
                    SELECT id FROM %s
                    WHERE (status = 'NEW' AND next_attempt_at <= now())
                       OR (status = 'PROCESSING' AND locked_until <= now())
                    ORDER BY created_at, id FOR UPDATE SKIP LOCKED LIMIT 1
                )
                UPDATE %s q SET status = 'PROCESSING', lock_token = ?,
                    locked_until = now() + (? * interval '1 second'), attempts = attempts + 1
                FROM candidate c WHERE q.id = c.id
                RETURNING q.id, lock_token, payload::text, attempts, %s
                """.formatted("telegram_notifications", "telegram_notifications", extra),
                (rs, row) -> new Job(rs.getObject("id", UUID.class),
                        rs.getObject("lock_token", UUID.class), rs.getString("payload"),
                        rs.getInt("attempts"), rs.getString("message_key"),
                        rs.getObject("chat_id", Long.class), rs.getObject("binding_id", UUID.class)),
                UUID.randomUUID(), properties.leaseSeconds());
    }

    public void sent(Job job) {
        finish(job, "SENT");
    }

    public void skip(Job job) {
        finish(job, "SKIPPED");
    }

    private void finish(Job job, String status) {
        jdbc.update("UPDATE telegram_notifications SET status = ?, sent_at = now(), "
                + "locked_until = NULL, lock_token = NULL WHERE id = ? "
                + "AND status = 'PROCESSING' AND lock_token = ?", status, job.id(), job.token());
    }

    public void failed(Job job, String safeError, boolean permanent, long retryAfter) {
        String state = permanent || job.attempts() >= properties.maxAttempts() ? "FAILED" : "NEW";
        long delay = Math.max(properties.delay(job.attempts()), retryAfter);
        jdbc.update("UPDATE telegram_notifications SET status = ?, last_error = ?, "
                + "next_attempt_at = now() + (? * interval '1 second'), "
                + "locked_until = NULL, lock_token = NULL WHERE id = ? "
                + "AND status = 'PROCESSING' AND lock_token = ?",
                state, safeError, delay, job.id(), job.token());
    }
}
