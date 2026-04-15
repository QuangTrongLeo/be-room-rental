package tmdt.be_room_rental.mapper.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.AmenityResponse;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Amenity;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.repository.post.AmenityRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    public PostResponse toResponse(Post post) {
        if (post == null) return null;
        
        List<AmenityResponse> amenityResponses = null;
        if (post.getAmenities() != null && !post.getAmenities().isEmpty()) {
            List<Amenity> amenityEntities = amenityRepository.findAllById(post.getAmenities());
            amenityResponses = amenityMapper.toResponseList(amenityEntities);
        }

        return PostResponse.builder()
                .id(post.getId())
                .landlordId(post.getLandlordId())
                .title(post.getTitle())
                .content(post.getContent())
                .price(post.getPrice())
                .status(post.getStatus())
                .isBoosted(post.getIsBoosted())
                .views(post.getViews())
                .favorites(post.getFavorites())
                .createdAt(post.getCreatedAt())
                .address(post.getAddress())
                .area(post.getArea())
                .images(post.getImages())
                .roomType(post.getRoomType())
                .longitude(post.getLocation() != null ? post.getLocation().getX() : null)
                .latitude(post.getLocation() != null ? post.getLocation().getY() : null)
                .amenities(amenityResponses)
                .build();
    }

    public List<PostResponse> toResponseList(List<Post> posts) {
        if (posts == null) return List.of();
        return posts.stream().map(this::toResponse).collect(Collectors.toList());
    }
}