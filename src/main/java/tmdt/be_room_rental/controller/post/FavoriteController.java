package tmdt.be_room_rental.controller.post;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.post.FavoriteResponse;
import tmdt.be_room_rental.service.interfaces.post.IFavoriteService;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping("/{postId}")
    @PreAuthorize("isAuthenticated()") // User hoặc Landlord đều có thể
    public ApiResponse<FavoriteResponse> addFavorite(@PathVariable String postId) {
        return ApiResponse.<FavoriteResponse>builder()
                .code(200)
                .message("Thêm bài đăng vào danh sách yêu thích thành công.")
                .data(favoriteService.addFavorite(postId))
                .build();
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFavorite(@PathVariable String postId) {
        favoriteService.removeFavorite(postId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa bài đăng khỏi danh sách yêu thích.")
                .build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FavoriteResponse>> getMyFavorites() {
        return ApiResponse.<List<FavoriteResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng yêu thích thành công.")
                .data(favoriteService.getMyFavorites())
                .build();
    }

    @GetMapping("/{postId}/check")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> checkFavorite(@PathVariable String postId) {
        return ApiResponse.<Boolean>builder()
                .code(200)
                .message("Kiểm tra trạng thái yêu thích.")
                .data(favoriteService.isFavorited(postId))
                .build();
    }
}
