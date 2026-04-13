package tmdt.be_room_rental.dto.res.post;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.PostStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {
    private String id;
    private String roomId;
    private String landlordId;
    private String title;
    private String content;
    private double price;
    private PostStatus status;
    private boolean isBoosted;
    private int views;
    private int favorites;
    private LocalDateTime createdAt;
}
