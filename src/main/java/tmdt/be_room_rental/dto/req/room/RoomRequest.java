package tmdt.be_room_rental.dto.req.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import tmdt.be_room_rental.enums.RoomType;

import java.util.List;

@Data
public class RoomRequest {
    private String address;
    private double area;
    private List<String> amenities;
    private List<MultipartFile> images;
    private RoomType roomType;
    private Double latitude;
    private Double longitude;
}
