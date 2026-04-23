package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.AmenityRequest;
import tmdt.be_room_rental.dto.res.post.AmenityResponse;
import tmdt.be_room_rental.entity.Amenity;
import tmdt.be_room_rental.mapper.post.AmenityMapper;
import tmdt.be_room_rental.repository.post.AmenityRepository;
import tmdt.be_room_rental.service.interfaces.post.IAmenityService;

import java.time.LocalDateTime;
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
                .createdAt(LocalDateTime.now())
                .build();

        Amenity savedAmenity = amenityRepository.save(amenity);

        return amenityMapper.toResponse(savedAmenity);
    }

    @Override
    public AmenityResponse updateAmenity(String id, AmenityRequest request) {
        Amenity amenity = findAmenityById(id);
        amenity.setName(request.getName());
        return amenityMapper.toResponse(amenityRepository.save(amenity));
    }

    @Override
    public AmenityResponse getAmenityById(String id) {
        return amenityMapper.toResponse(findAmenityById(id));
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
        return amenityMapper.toResponseList(amenityRepository.findAllByOrderByCreatedAtDesc());
    }

    public Amenity findAmenityById(String id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tiện nghi không tồn tại với ID: " + id));
    }
}
