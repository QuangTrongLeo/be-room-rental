package tmdt.be_room_rental.dto.res.room;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AmenityResponse {
    private String id;
    private String name;
    private LocalDateTime createdAt;
}
