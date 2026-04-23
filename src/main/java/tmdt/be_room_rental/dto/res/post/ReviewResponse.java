package tmdt.be_room_rental.dto.res.post;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private String id;
    private String userId;
    private String landlordId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
