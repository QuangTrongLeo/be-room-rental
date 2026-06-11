package tmdt.be_room_rental.dto.res.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tmdt.be_room_rental.enums.status.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String id;
    private String userId;
    private String landlordId;
    private String postId;
    private PostResponse post;

    // Thông tin tenant — để landlord thấy ai đặt phòng
    private String tenantName;
    private String tenantAvatar;

    private LocalDateTime bookingTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
