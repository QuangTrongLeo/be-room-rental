package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.req.post.ReviewRequest;
import tmdt.be_room_rental.dto.res.post.ReviewResponse;

import java.util.List;

public interface IReviewService {
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse updateReview(String id, ReviewRequest request);
    void deleteReview(String id);
    List<ReviewResponse> getReviewsByLandlord(String landlordId);
}
