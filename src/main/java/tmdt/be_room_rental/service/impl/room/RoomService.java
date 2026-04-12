package tmdt.be_room_rental.service.impl.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.mapper.room.RoomMapper;
import tmdt.be_room_rental.service.interfaces.room.IRoomService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {
    private final RoomMapper roomMapper;

    @Override
    public List<RoomTypeResponse> getRoomTypes() {
        return roomMapper.toTypeResponseList();
    }
}
