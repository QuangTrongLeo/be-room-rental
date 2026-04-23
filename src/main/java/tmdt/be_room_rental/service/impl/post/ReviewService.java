package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.ReviewRequest;
import tmdt.be_room_rental.dto.res.post.ReviewResponse;
import tmdt.be_room_rental.entity.Review;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.mapper.post.ReviewMapper;
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

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        // 1. Lấy user đang đăng nhập
        User currentUser = securityService.getCurrentUser();

        // 2. Khởi tạo thực thể Review
        Review review = Review.builder()
                .userId(currentUser.getId())
                .landlordId(request.getLandlordId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Lưu và trả về DTO thông qua mapper
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponse updateReview(String id, ReviewRequest request) {
        // 1. Tìm review
        Review review = findReviewById(id);
        User currentUser = securityService.getCurrentUser();

        // 2. Kiểm tra quyền
        if (!review.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa đánh giá này");
        }

        // 3. Cập nhật thông tin bằng cách kiểm tra if
        boolean isChanged = false;

        if (request.getRating() > 0) {
            review.setRating(request.getRating());
            isChanged = true;
        }

        if (request.getComment() != null && !request.getComment().trim().isEmpty()) {
            review.setComment(request.getComment());
            isChanged = true;
        }

        // 4. Nếu có bất kỳ sự thay đổi nào, cập nhật lại thời gian
        if (isChanged) {
            review.setCreatedAt(LocalDateTime.now());
        }

        // 5. Lưu và trả về
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(String id) {
        // 1. Tìm review
        Review review = findReviewById(id);
        User currentUser = securityService.getCurrentUser();

        // 2. Kiểm tra quyền: Chỉ người viết hoặc ADMIN mới được xóa
        if (!review.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByLandlord(String landlordId) {
        List<Review> reviews = reviewRepository.findAllByLandlordIdOrderByCreatedAtDesc(landlordId);
        return reviewMapper.toResponseList(reviews);
    }

    private Review findReviewById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại với ID: " + id));
    }
}