package studying.notification;

import java.time.LocalDateTime;

public interface Notification {
    String getText();
    LocalDateTime getCreatedAt();
    Priority getPriority();
    String getDeliveryDetails();
}
