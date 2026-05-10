package tmdt.be_room_rental.dto.res.notification;

import lombok.*;
import tmdt.be_room_rental.enums.type.NotificationType;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String recipientId;
    private String senderId;
    private String senderName;
    private NotificationType type;
    private String title;
    private String message;
    private String refId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
