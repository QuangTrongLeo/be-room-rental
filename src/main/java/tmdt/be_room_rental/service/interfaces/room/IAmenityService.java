package tmdt.be_room_rental.service.interfaces.room;

import tmdt.be_room_rental.dto.req.room.AmenityRequest;
import tmdt.be_room_rental.dto.res.room.AmenityResponse;

import java.util.List;

public interface IAmenityService {
    AmenityResponse createAmenity(AmenityRequest request);
    AmenityResponse updateAmenity(AmenityRequest request);
    void deleteAmenity(String id);
    List<AmenityResponse> getAmenities();
}
