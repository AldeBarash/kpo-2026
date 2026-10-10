package studying.notification.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.exception.NotificationValidationException;

/** Unit tests for the Lombok builder of a notification draft. */
@DisplayName("Тесты Builder для черновика уведомления")
class NotificationDraftTest {
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 10, 1, 10, 15);

    @Test
    @DisplayName("Builder создаёт черновик со всеми переданными полями")
    void buildsDraftWithAllFields() {
        final NotificationDraft draft = NotificationDraft.builder()
                .channel(NotificationChannel.EMAIL)
                .recipient("student@example.org")
                .text("Ваш заказ передан в доставку")
                .createdAt(CREATED_AT)
                .priority(NotificationPriority.HIGH)
                .build();

        assertEquals(NotificationChannel.EMAIL, draft.channel());
        assertEquals("student@example.org", draft.recipient());
        assertEquals("Ваш заказ передан в доставку", draft.text());
        assertEquals(CREATED_AT, draft.createdAt());
        assertEquals(NotificationPriority.HIGH, draft.priority());
    }

    @Test
    @DisplayName("Builder отклоняет черновик с пустым текстом")
    void rejectsBlankText() {
        assertThrows(NotificationValidationException.class,
                () -> NotificationDraft.builder()
                        .channel(NotificationChannel.PUSH)
                        .recipient("device-42")
                        .text("   ")
                        .createdAt(CREATED_AT)
                        .priority(NotificationPriority.NORMAL)
                        .build());
    }
}
