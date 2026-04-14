package tmdt.be_room_rental.mapper.room;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.room.RoomResponse;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.entity.Room;
import tmdt.be_room_rental.enums.type.RoomType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        if (room == null) return null;

        return RoomResponse.builder()
                .id(room.getId())
                .landlordId(room.getLandlordId())
                .address(room.getAddress())
                .area(room.getArea())
                .amenities(room.getAmenities())
                .images(room.getImages())
                .roomType(room.getRoomType())
                .longitude(room.getLocation() != null ? room.getLocation().getX() : null)
                .latitude(room.getLocation() != null ? room.getLocation().getY() : null)
                .createdAt(room.getCreatedAt())
                .build();
    }

    public List<RoomResponse> toResponseList(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return Collections.emptyList();
        }

        return rooms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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
