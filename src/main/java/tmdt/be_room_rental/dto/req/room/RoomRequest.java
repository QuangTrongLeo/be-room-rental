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
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    @Positive(message = "Diện tích phải lớn hơn 0")
    private double area;

    private List<String> amenities;
    private List<MultipartFile> images;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType roomType;

    private Double longitude;
    private Double latitude;
}
