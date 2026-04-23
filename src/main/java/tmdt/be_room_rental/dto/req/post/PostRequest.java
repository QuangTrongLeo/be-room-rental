package tmdt.be_room_rental.dto.req.post;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.enums.type.RoomType;

import java.util.List;

@Data
public class PostRequest {
    private String title;
    private String content;
    private String address;
    private Double price;
    private Double area;
    private Double latitude;
    private Double longitude;
    private PostStatus status;
    private RoomType roomType;
    private Boolean isBoosted;
    private List<MultipartFile> images;
    private List<String> amenities;
}
