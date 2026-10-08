package studying.notification;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class PushNotification implements Notification {
    String text;
    LocalDateTime createdAt;
    Priority priority;
    String deviceId;

    @Override
    public String getDeliveryDetails() {
        return "Push to Device ID: " + deviceId;
    }
}
