package tmdt.be_room_rental.dto.req.room;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import tmdt.be_room_rental.enums.type.RoomType;

import java.util.List;

@Data
public class RoomRequest {
    private String address;
    private Double area;
    private List<String> amenities;
    private List<MultipartFile> images;
    private RoomType roomType;
    private Double latitude;
    private Double longitude;
}
