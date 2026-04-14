package tmdt.be_room_rental.mapper.post;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.ReviewResponse;
import tmdt.be_room_rental.entity.Review;

import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .landlordId(review.getLandlordId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        if (reviews == null) {
            return List.of();
        }

        return reviews.stream()
                .map(this::toResponse)
                .toList();
    }
}