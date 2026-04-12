package tmdt.be_room_rental.service.interfaces.room;

import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;

import java.util.List;

public interface IRoomService {
    List<RoomTypeResponse> getRoomTypes();
}
