package tmdt.be_room_rental.service.impl.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.room.AmenityRequest;
import tmdt.be_room_rental.dto.res.room.AmenityResponse;
import tmdt.be_room_rental.entity.Amenity;
import tmdt.be_room_rental.mapper.room.AmenityMapper;
import tmdt.be_room_rental.repository.room.AmenityRepository;
import tmdt.be_room_rental.service.interfaces.room.IAmenityService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService implements IAmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {
        if (amenityRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tiện nghi này đã tồn tại");
        }

        Amenity amenity = Amenity.builder()
                .name(request.getName())
                .build();

        return amenityMapper.toResponse(amenityRepository.save(amenity));
    }

    @Override
    public AmenityResponse updateAmenity(AmenityRequest request) {
        Amenity amenity = findAmenityById(request.getId());
        amenity.setName(request.getName());
        return amenityMapper.toResponse(amenityRepository.save(amenity));
    }

    @Override
    public void deleteAmenity(String id) {
        if (!amenityRepository.existsById(id)) {
            throw new RuntimeException("Tiện nghi không tồn tại");
        }
        amenityRepository.deleteById(id);
    }

    @Override
    public List<AmenityResponse> getAmenities() {
        return amenityMapper.toResponseList(amenityRepository.findAll());
    }

    public Amenity findAmenityById(String id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tiện nghi không tồn tại với ID: " + id));
    }
}
