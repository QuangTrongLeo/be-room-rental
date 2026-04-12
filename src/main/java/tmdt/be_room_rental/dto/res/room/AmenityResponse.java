package tmdt.be_room_rental.dto.res.room;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmenityResponse {
    private String id;
    private String name;
}
