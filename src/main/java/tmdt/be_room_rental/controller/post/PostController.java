package tmdt.be_room_rental.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.service.interfaces.post.IPostService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<PostResponse> createPost(@RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Tạo bài đăng thành công.")
                .data(postService.createPost(request))
                .build();
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<PostResponse>> getMyPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng của bạn thành công.")
                .data(postService.getMyPosts())
                .build();
    }

    @GetMapping("/active")
    public ApiResponse<List<PostResponse>> getActivePosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng đang hiển thị thành công.")
                .data(postService.getActivePosts())
                .build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PostResponse>> getPendingPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng chờ duyệt thành công.")
                .data(postService.getPendingPosts())
                .build();
    }

    @GetMapping("/hidden")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<PostResponse>> getHiddenPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng đã ẩn thành công.")
                .data(postService.getHiddenPosts())
                .build();
    }

    @GetMapping("/rejected")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<PostResponse>> getRejectPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy danh sách bài đăng bị từ chối thành công.")
                .data(postService.getRejectPosts())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PostResponse>> getPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Lấy toàn bộ danh sách bài đăng thành công.")
                .data(postService.getPosts())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPostById(@PathVariable String id) {
        return ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Lấy chi tiết bài đăng thành công.")
                .data(postService.getPostById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<PostResponse> updatePost(@PathVariable String id, @RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Cập nhật bài đăng thành công.")
                .data(postService.updatePost(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa bài đăng thành công.")
                .build();
    }
}