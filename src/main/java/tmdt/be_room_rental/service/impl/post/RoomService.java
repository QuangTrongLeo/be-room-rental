package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.post.RoomTypeResponse;
import tmdt.be_room_rental.mapper.post.RoomMapper;
import tmdt.be_room_rental.service.interfaces.post.IRoomService;

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