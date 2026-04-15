package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.res.post.RoomTypeResponse;
import java.util.List;

public interface IRoomService {
    List<RoomTypeResponse> getRoomTypes();
}