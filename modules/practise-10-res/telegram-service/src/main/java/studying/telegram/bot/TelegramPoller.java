package studying.telegram.bot;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramPoller {
    private final TelegramProperties properties;
    private final TelegramClient telegram;
    private final TelegramUpdates updates;
    private final JdbcTemplate jdbc;

    @Scheduled(fixedDelayString = "${app.telegram.poll-ms:1000}")
    public void poll() {
        if (!properties.enabled()) { return; }
        UUID token = UUID.randomUUID();
        var offsets = jdbc.queryForList("""
                UPDATE telegram_poll_state SET lock_token = ?, locked_until = now() + interval '60 seconds'
                WHERE id = 1 AND (locked_until IS NULL OR locked_until <= now()) RETURNING next_offset
                """, Long.class, token);
        if (offsets.isEmpty()) { return; }
        long retryDelay = 0;
        try {
            long offset = offsets.getFirst();
            for (var update : telegram.updates(offset)) {
                // Renew before each DB operation and verify ownership after a slow poll.
                int owned = jdbc.update("UPDATE telegram_poll_state SET locked_until = now() + interval '60 seconds' "
                        + "WHERE id = 1 AND lock_token = ? AND locked_until > now()", token);
                if (owned == 0) { return; }
                updates.handle(update);
                if (update.path("update_id").isIntegralNumber()) {
                    jdbc.update("UPDATE telegram_poll_state SET next_offset = ? WHERE id = 1 AND lock_token = ?",
                            update.path("update_id").asLong() + 1, token);
                }
            }
        } catch (TelegramClient.Failure failure) {
            log.warn("Telegram polling failed: {}", failure.getMessage());
            retryDelay = Math.max(5, failure.retryAfter());
        } finally {
            // Release only our lease, retaining a server-requested delay on failure.
            jdbc.update("""
                    UPDATE telegram_poll_state SET lock_token = NULL,
                        locked_until = CASE WHEN ? = 0 THEN NULL
                            ELSE now() + (? * interval '1 second') END
                    WHERE id = 1 AND lock_token = ?
                    """, retryDelay, retryDelay, token);
        }
    }
}
