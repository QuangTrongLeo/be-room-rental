package tmdt.be_room_rental.mapper.room;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.enums.RoomType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {
    public RoomTypeResponse toTypeResponse(RoomType roomType) {
        if (roomType == null) return null;

        return RoomTypeResponse.builder()
                .value(roomType.name())
                .name(roomType.getName())
                .build();
    }

    public List<RoomTypeResponse> toTypeResponseList() {
        return Arrays.stream(RoomType.values())
                .map(this::toTypeResponse)
                .collect(Collectors.toList());
    }
}
