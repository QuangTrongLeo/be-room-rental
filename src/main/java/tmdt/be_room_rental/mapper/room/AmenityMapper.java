package tmdt.be_room_rental.mapper.room;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.room.AmenityResponse;
import tmdt.be_room_rental.entity.Amenity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AmenityMapper {

    public AmenityResponse toResponse(Amenity amenity) {
        if (amenity == null) return null;

        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .build();
    }

    public List<AmenityResponse> toResponseList(List<Amenity> amenities) {
        if (amenities == null) return List.of();

        return amenities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}