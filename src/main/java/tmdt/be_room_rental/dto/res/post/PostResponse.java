package tmdt.be_room_rental.dto.res.post;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.enums.type.RoomType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private String id;
    private String landlordId;
    private String title;
    private String content;
    private String address;
    private Double price;
    private Double area;
    private Double latitude;
    private Double longitude;
    private Integer views;
    private Integer favorites;
    private PostStatus status;
    private RoomType roomType;
    private Boolean isBoosted;
    private List<String> images;
    private List<String> amenities;
    private LocalDateTime createdAt;
}
