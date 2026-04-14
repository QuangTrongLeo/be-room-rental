package tmdt.be_room_rental.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.post.ReviewRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.post.ReviewResponse;
import tmdt.be_room_rental.service.interfaces.post.IReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD', 'ADMIN')")
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .code(200)
                .message("Gửi đánh giá thành công.")
                .data(reviewService.createReview(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD', 'ADMIN')")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable String id,
            @RequestBody @Valid ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .code(200)
                .message("Cập nhật đánh giá thành công.")
                .data(reviewService.updateReview(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD', 'ADMIN')")
    public ApiResponse<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa đánh giá thành công.")
                .build();
    }

    @GetMapping("/landlord/{landlordId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByLandlord(@PathVariable String landlordId) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .code(200)
                .message("Lấy danh sách đánh giá thành công.")
                .data(reviewService.getReviewsByLandlord(landlordId))
                .build();
    }
}