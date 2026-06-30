package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.ReviewRequest;
import tmdt.be_room_rental.dto.res.post.ReviewResponse;
import tmdt.be_room_rental.entity.Review;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.BookingStatus;
import tmdt.be_room_rental.mapper.post.ReviewMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.BookingRepository;
import tmdt.be_room_rental.repository.post.ReviewRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.post.IReviewService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        User currentUser = requireCurrentUser();
        User landlord = requireLandlord(request.getLandlordId());

        if (!bookingRepository.existsByUserIdAndLandlordIdAndStatus(
                currentUser.getId(), landlord.getId(), BookingStatus.RENTED)) {
            throw new RuntimeException("Ban chi co the danh gia chu tro sau khi da xac nhan thue tro.");
        }
        if (reviewRepository.existsByUserIdAndLandlordId(currentUser.getId(), landlord.getId())) {
            throw new RuntimeException("Ban da danh gia chu tro nay roi.");
        }

        Review review = Review.builder()
                .userId(currentUser.getId())
                .landlordId(landlord.getId())
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .createdAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        refreshLandlordRating(landlord.getId());
        return reviewMapper.toResponse(saved);
    }

    @Override
    public ReviewResponse updateReview(String id, ReviewRequest request) {
        Review review = findReviewById(id);
        User currentUser = requireCurrentUser();

        if (!review.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Ban khong co quyen chinh sua danh gia nay");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        refreshLandlordRating(saved.getLandlordId());
        return reviewMapper.toResponse(saved);
    }

    @Override
    public void deleteReview(String id) {
        Review review = findReviewById(id);
        User currentUser = requireCurrentUser();

        if (!review.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Ban khong co quyen xoa danh gia nay");
        }

        String landlordId = review.getLandlordId();
        reviewRepository.delete(review);
        refreshLandlordRating(landlordId);
    }

    @Override
    public List<ReviewResponse> getReviewsByLandlord(String landlordId) {
        List<Review> reviews = reviewRepository.findAllByLandlordIdOrderByCreatedAtDesc(landlordId);
        return reviewMapper.toResponseList(reviews);
    }

    private Review findReviewById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh gia khong ton tai voi ID: " + id));
    }

    private User requireCurrentUser() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Ban can dang nhap de su dung chuc nang danh gia.");
        }
        return currentUser;
    }

    private User requireLandlord(String landlordId) {
        if (landlordId == null || landlordId.trim().isEmpty()) {
            throw new RuntimeException("Landlord id is required");
        }
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new RuntimeException("Chu tro khong ton tai"));
        if (landlord.getRole() != RoleEnum.LANDLORD) {
            throw new RuntimeException("Nguoi duoc danh gia phai la LANDLORD");
        }
        return landlord;
    }

    private void refreshLandlordRating(String landlordId) {
        User landlord = userRepository.findById(landlordId).orElse(null);
        if (landlord == null) {
            return;
        }

        List<Review> reviews = reviewRepository.findAllByLandlordIdOrderByCreatedAtDesc(landlordId);
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        landlord.setRating(average);
        userRepository.save(landlord);
    }
}
