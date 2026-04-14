package tmdt.be_room_rental.dto.req.post;

import lombok.Data;
import tmdt.be_room_rental.enums.PostStatus;

@Data
public class PostRequest {
    private String roomId;
    private String title;
    private String content;
    private Double price;
    // PENDING, ACTIVE, REJECTED, EXPIRED, HIDDEN
    private PostStatus status;
    private Boolean isBoosted;
}
