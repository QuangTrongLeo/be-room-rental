package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.req.post.AmenityRequest;
import tmdt.be_room_rental.dto.res.post.AmenityResponse;

import java.util.List;

public interface IAmenityService {
    AmenityResponse createAmenity(AmenityRequest request);
    AmenityResponse updateAmenity(String id, AmenityRequest request);
    AmenityResponse getAmenityById(String id);
    void deleteAmenity(String id);
    List<AmenityResponse> getAmenities();
}
