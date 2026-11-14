package studying.telegram.bot;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class TelegramUpdates {
    private final TelegramCommands commands;
    private final GatewayClient gateway;

    /** HTTP happens before the local transaction. Redemption is replay-safe in Gateway. */
    public void handle(JsonNode update) {
        if (!update.path("update_id").isIntegralNumber()) { return; }
        long updateId = update.path("update_id").asLong();
        if (commands.handled(updateId)) { return; }
        JsonNode message = update.path("message");
        if (!"private".equals(message.path("chat").path("type").asText())
                || !message.path("from").path("id").isIntegralNumber()
                || !message.path("chat").path("id").isIntegralNumber()
                || message.path("from").path("is_bot").asBoolean(false)) {
            commands.ignore(updateId); return;
        }
        long sender = message.path("from").path("id").asLong();
        long chat = message.path("chat").path("id").asLong();
        if (sender <= 0 || chat != sender) { commands.ignore(updateId); return; }
        String[] parts = message.path("text").asText("").strip().split("\\s+", 2);
        String command = parts[0].split("@", 2)[0];
        UUID user = null;
        TelegramCommands.Binding binding = null;
        String response;
        try {
            switch (command) {
                case "/start", "/help" -> response = "Создайте код через POST /api/telegram/link-codes с API-ключом. "
                        + "Отправьте /link <code> в течение пяти минут.\n/balance — баланс; /unlink — отключить.";
                case "/link" -> {
                    response = "Используйте /link <code>. API-ключ сюда отправлять не нужно.";
                    if (parts.length == 2) { user = gateway.redeem(updateId, sender, chat, parts[1].strip()); }
                }
                case "/balance" -> {
                    binding = commands.binding(sender, chat);
                    response = binding == null ? "Нет активной привязки. Инструкция: /help."
                            : gateway.balance(binding.userId());
                }
                case "/unlink" -> response = "Привязка отключена.";
                default -> response = "Неизвестная команда. Список команд: /help.";
            }
        } catch (GatewayClient.Rejected rejected) { response = rejected.getMessage(); }
        // Unavailable propagates: no update or reply is committed, poller retries later.
        commands.complete(updateId, sender, chat, command, response, user, binding);
    }
}
