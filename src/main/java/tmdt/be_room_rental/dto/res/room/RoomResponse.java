package tmdt.be_room_rental.dto.res.room;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.RoomType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomResponse {
    private String id;
    private String landlordId;
    private String address;
    private double area;
    private List<String> amenities;
    private List<String> images;
    private RoomType roomType;
    private Double longitude;
    private Double latitude;
    private LocalDateTime createdAt;
}
