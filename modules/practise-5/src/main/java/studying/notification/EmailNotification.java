package studying.notification;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class EmailNotification implements Notification {
    String text;
    LocalDateTime createdAt;
    Priority priority;
    String emailAddress;
    String subject;

    @Override
    public String getDeliveryDetails() {
        return "Email to: " + emailAddress + ", Subject: " + subject;
    }
}
