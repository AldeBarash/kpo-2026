package studying.telegram.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import studying.telegram.events.DeliveryQueue;
import studying.telegram.events.NotificationInbox;

@Component
@RequiredArgsConstructor
public class TelegramNotificationWorker {
    private final TelegramProperties properties;
    private final DeliveryQueue queue;
    private final TelegramClient telegram;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    @Scheduled(fixedDelayString = "${app.telegram.send-ms:1000}")
    public void send() {
        if (!properties.enabled()) { return; }
        for (var job : queue.claim()) {
            if (job.bindingId() != null && !Boolean.TRUE.equals(jdbc.queryForObject("""
                    SELECT EXISTS(SELECT 1 FROM telegram_bindings b
                    WHERE b.id = ? AND b.chat_id = ?)
                    """, Boolean.class, job.bindingId(), job.chatId()))) {
                queue.skip(job);
                continue;
            }
            try {
                var message = json.readValue(job.payload(), NotificationInbox.Message.class);
                telegram.send(job.chatId(), message.text());
                queue.sent(job);
            } catch (TelegramClient.Failure failure) {
                queue.failed(job, failure.getMessage(),
                        failure.permanent(), failure.retryAfter());
            } catch (Exception exception) {
                queue.failed(job, "NOTIFICATION_FAILED", false, 0);
            }
        }
    }
}
