package studying.telegram.bot;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import studying.telegram.events.NotificationInbox;

@Service
@RequiredArgsConstructor
public class TelegramCommands {
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    public record Binding(UUID id, UUID userId) { }

    public boolean handled(long update) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM telegram_updates WHERE update_id = ?)", Boolean.class, update));
    }

    public Binding binding(long sender, long chat) {
        var rows = jdbc.query("SELECT id, user_id FROM telegram_bindings WHERE telegram_user_id = ? AND chat_id = ?",
                (rs, n) -> new Binding(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class)), sender, chat);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public void ignore(long update) {
        jdbc.update("INSERT INTO telegram_updates (update_id) VALUES (?) ON CONFLICT DO NOTHING", update);
    }

    @Transactional
    public void complete(long update, long sender, long chat, String command,
            String response, UUID userId, Binding expected) {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)", sender);
        if (jdbc.update("INSERT INTO telegram_updates (update_id) VALUES (?) ON CONFLICT DO NOTHING", update) == 0) { return; }
        if ("/link".equals(command) && userId != null) {
            jdbc.update("""
                    INSERT INTO telegram_bindings (id, user_id, telegram_user_id, chat_id)
                    VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING
                    """, UUID.randomUUID(), userId, sender, chat);
            Binding linked = binding(sender, chat);
            response = linked != null && linked.userId().equals(userId)
                    ? "Привязка сохранена. Баланс: /balance. Отключение: /unlink."
                    : "Аккаунт уже привязан. Сначала выполните /unlink в прежнем чате.";
        } else if ("/unlink".equals(command)) {
            jdbc.update("DELETE FROM telegram_bindings WHERE telegram_user_id = ? AND chat_id = ?", sender, chat);
            response = "Привязка отключена. Новые уведомления отправляться не будут.";
        } else if ("/balance".equals(command) && expected != null) {
            Binding current = binding(sender, chat);
            if (current == null || !current.id().equals(expected.id())) {
                response = "Привязка изменилась. Повторите /balance.";
            }
        }
        jdbc.update("INSERT INTO telegram_notifications (id, update_id, chat_id, payload) VALUES (?, ?, ?, ?::jsonb)",
                UUID.randomUUID(), update, chat, json.writeValueAsString(new NotificationInbox.Message(response)));
    }
}
