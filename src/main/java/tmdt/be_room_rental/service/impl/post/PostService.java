package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.Room;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.PostStatus;
import tmdt.be_room_rental.mapper.post.PostMapper;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.repository.room.RoomRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.room.RoomService;
import tmdt.be_room_rental.service.interfaces.post.IPostService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final RoomService roomService;
    private final SecurityService securityService;
    private final PostMapper postMapper;

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();

        // 1. Kiểm tra phòng có tồn tại không
        Room room = roomService.findRoomById(request.getRoomId());

        // 2. Kiểm tra phòng có thuộc về chủ trọ đang đăng nhập không
        if (!room.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền đăng bài cho phòng này");
        }

        // 3. Khởi tạo Post
        Post post = Post.builder()
                .roomId(request.getRoomId())
                .landlordId(currentUser.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.PENDING)
                .isBoosted(request.getIsBoosted() != null ? request.getIsBoosted() : false)
                .createdAt(LocalDateTime.now())
                .build();

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updatePost(String id, PostRequest request) {
        Post post = findPostById(id);
        User currentUser = securityService.getCurrentUser();

        // Kiểm tra quyền sở hữu bài đăng
        if (!post.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        }

        // Cập nhật các trường thông tin
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getPrice() != null) post.setPrice(request.getPrice());
        if (request.getStatus() != null) post.setStatus(request.getStatus());
        if (request.getIsBoosted() != null) post.setIsBoosted(request.getIsBoosted());

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updateStatusPost(String id, PostRequest request) {
        // 1. Tìm bài đăng
        Post post = findPostById(id);

        // 2. Kiểm tra status truyền lên có hợp lệ không
        if (request.getStatus() == null) {
            throw new RuntimeException("Trạng thái (status) không được để trống");
        }

        // 3. Cập nhật trạng thái mới
        post.setStatus(request.getStatus());

        // 4. Lưu vào database
        Post updatedPost = postRepository.save(post);

        // 5. Trả về kết quả qua Mapper
        return postMapper.toResponse(updatedPost);
    }

    @Override
    public PostResponse getPostById(String id) {
        Post post = findPostById(id);

        // Tăng view mỗi khi có người xem chi tiết
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        return postMapper.toResponse(post);
    }

    @Override
    public List<PostResponse> getMyPosts() {
        User currentUser = securityService.getCurrentUser();
        List<Post> posts = postRepository.findAllByLandlordIdOrderByCreatedAtDesc(currentUser.getId());
        return postMapper.toResponseList(posts);
    }

    @Override
    public List<PostResponse> getPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return postMapper.toResponseList(posts);
    }

    @Override
    public List<PostResponse> getPendingPosts() {
        List<Post> posts = postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
        return postMapper.toResponseList(posts);
    }

    @Override
    public List<PostResponse> getActivePosts() {
        List<Post> posts = postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE);
        return postMapper.toResponseList(posts);
    }

    @Override
    public List<PostResponse> getHiddenPosts() {
        List<Post> posts = postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.HIDDEN);
        return postMapper.toResponseList(posts);
    }

    @Override
    public List<PostResponse> getRejectPosts() {
        List<Post> posts = postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.REJECTED);
        return postMapper.toResponseList(posts);
    }

    @Override
    public void deletePost(String id) {
        Post post = findPostById(id);
        User currentUser = securityService.getCurrentUser();

        if (!post.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa bài đăng này");
        }

        postRepository.delete(post);
    }

    public Post findPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại với ID: " + id));
    }
}