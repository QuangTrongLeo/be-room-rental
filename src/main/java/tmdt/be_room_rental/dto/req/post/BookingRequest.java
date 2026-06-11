package tmdt.be_room_rental.dto.req.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private String postId;
    private LocalDateTime bookingTime;
}
