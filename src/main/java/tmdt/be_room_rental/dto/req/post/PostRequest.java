package tmdt.be_room_rental.dto.req.post;

import lombok.Data;
import tmdt.be_room_rental.enums.PostStatus;

@Data
public class PostRequest {
    private String roomId;
    private String title;
    private String content;
    private double price;
    private PostStatus status;
    private boolean isBoosted;
}
