package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.type.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notifications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed
    private String recipientId;   // Người nhận thông báo

    private String senderId;      // Người gửi (tenant hoặc landlord)
    private String senderName;    // Tên người gửi

    private NotificationType type; // BOOKING_CREATED, BOOKING_APPROVED, BOOKING_REJECTED, BOOKING_CANCELLED

    private String title;
    private String message;
    private String refId;          // bookingId để FE navigate

    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime createdAt;
}
