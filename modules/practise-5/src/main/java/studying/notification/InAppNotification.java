package studying.notification;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class InAppNotification implements Notification {
    String text;
    LocalDateTime createdAt;
    Priority priority;
    String userId;

    @Override
    public String getDeliveryDetails() {
        return "In-App to User ID: " + userId;
    }
}
