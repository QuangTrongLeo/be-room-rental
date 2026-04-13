package tmdt.be_room_rental.service.interfaces.room;

import tmdt.be_room_rental.dto.req.room.RoomRequest;
import tmdt.be_room_rental.dto.res.room.RoomResponse;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;

import java.util.List;

public interface IRoomService {
    RoomResponse createRoom(RoomRequest request);
    RoomResponse updateRoom(String id, RoomRequest request);
    RoomResponse getRoomById(String id);
    List<RoomResponse> getRooms();
    List<RoomResponse> getRoomsByLandLord();
    void deleteRoom(String id);
    List<RoomTypeResponse> getRoomTypes();
}
