package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "bookings")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String userId;
    private String landlordId;
    private String postId;
    private LocalDateTime bookingTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
