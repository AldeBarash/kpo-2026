package studying.notification.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.notification.enums.NotificationChannel;
import studying.notification.enums.NotificationPriority;
import studying.notification.exception.NotificationValidationException;
import studying.notification.mapper.NotificationDraftMapper;
import studying.notification.validation.NotificationDraftValidator;

/** Модульные тесты Lombok Builder для черновика уведомления. */
@DisplayName("Тесты Builder для черновика уведомления")
class NotificationDraftTest {
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 10, 1, 10, 15);
    private final NotificationDraftMapper mapper =
            new NotificationDraftMapper(new NotificationDraftValidator());

    @Test
    @DisplayName("Маппер создаёт черновик со всеми переданными полями")
    void buildsDraftWithAllFields() {
        NotificationDraft draft = mapper.create(NotificationChannel.EMAIL,
                "student@example.org", "Ваш заказ передан в доставку",
                CREATED_AT, NotificationPriority.HIGH);

        assertEquals(NotificationChannel.EMAIL, draft.channel());
        assertEquals("student@example.org", draft.recipient());
        assertEquals("Ваш заказ передан в доставку", draft.text());
        assertEquals(CREATED_AT, draft.createdAt());
        assertEquals(NotificationPriority.HIGH, draft.priority());
    }

    @Test
    @DisplayName("Маппер отклоняет черновик с пустым текстом")
    void rejectsBlankText() {
        assertThrows(NotificationValidationException.class,
                () -> mapper.create(NotificationChannel.PUSH, "device-42",
                        "   ", CREATED_AT, NotificationPriority.NORMAL));
    }
}
