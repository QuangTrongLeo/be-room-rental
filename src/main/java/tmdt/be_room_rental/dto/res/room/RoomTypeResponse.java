package tmdt.be_room_rental.dto.res.room;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomTypeResponse {
    private String value;
    private String name;
}
