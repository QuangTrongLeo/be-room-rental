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
import tmdt.be_room_rental.service.interfaces.post.IPostService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final RoomRepository roomRepository;
    private final PostMapper postMapper;
    private final SecurityService securityService;

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();

        // 1. Kiểm tra phòng có tồn tại không
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

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
                .isBoosted(request.isBoosted())
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
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setPrice(request.getPrice());
        if (request.getStatus() != null) post.setStatus(request.getStatus());
        post.setBoosted(request.isBoosted());

        return postMapper.toResponse(postRepository.save(post));
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