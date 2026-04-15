package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.mapper.post.PostMapper;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.auth.ICloudinaryService;
import tmdt.be_room_rental.service.interfaces.post.IPostHistoryService;
import tmdt.be_room_rental.service.interfaces.post.IPostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final IPostHistoryService postHistoryService;
    private final ICloudinaryService cloudinaryService;
    private final SecurityService securityService;
    private final PostMapper postMapper;

    private static final int MAX_IMAGES = 8;

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();

        // Kiểm tra ảnh
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new RuntimeException("Chỉ được upload tối đa " + MAX_IMAGES + " ảnh");
        }

        Post post = Post.builder()
                .landlordId(currentUser.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .status(PostStatus.PENDING)
                .isBoosted(request.getIsBoosted() != null ? request.getIsBoosted() : false)
                .address(request.getAddress())
                .area(request.getArea())
                .amenities(request.getAmenities())
                .roomType(request.getRoomType())
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getLongitude() != null && request.getLatitude() != null) {
            post.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> urls = request.getImages().stream()
                    .map(file -> cloudinaryService.upload(file, "posts"))
                    .collect(Collectors.toList());
            post.setImages(urls);
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updatePost(String id, PostRequest request) {
        Post post = findPostById(id);
        User currentUser = securityService.getCurrentUser();

        if (!post.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        }

        // Update Post info
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getPrice() != null) post.setPrice(request.getPrice());

        // Update Room info (Flattened)
        if (request.getAddress() != null) post.setAddress(request.getAddress());
        if (request.getArea() != null && request.getArea() > 0) post.setArea(request.getArea());
        if (request.getAmenities() != null) post.setAmenities(request.getAmenities());
        if (request.getRoomType() != null) post.setRoomType(request.getRoomType());

        if (request.getLongitude() != null && request.getLatitude() != null) {
            post.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        // Logic xử lý ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            if (request.getImages().size() > MAX_IMAGES) throw new RuntimeException("Quá số lượng ảnh");
            if (post.getImages() != null) post.getImages().forEach(cloudinaryService::deleteByUrl);

            List<String> newUrls = request.getImages().stream()
                    .map(file -> cloudinaryService.upload(file, "posts"))
                    .collect(Collectors.toList());
            post.setImages(newUrls);
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updateStatusPost(String id, PostRequest request) {
        Post post = findPostById(id);
        if (request.getStatus() == null) throw new RuntimeException("Status trống");
        post.setStatus(request.getStatus());
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse getPostById(String id) {
        Post post = findPostById(id);
        post.setViews(post.getViews() + 1);

        try {
            String currentUserId = securityService.getCurrentUser().getId();
            if (currentUserId != null) {
                postHistoryService.saveHistory(currentUserId, id);
            }
        } catch (Exception e) {
            // User chưa đăng nhập thì bỏ qua không lưu lịch sử
        }
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public List<PostResponse> getMyPosts() {
        return postMapper.toResponseList(postRepository.findAllByLandlordIdOrderByCreatedAtDesc(securityService.getCurrentUser().getId()));
    }

    @Override
    public List<PostResponse> getPosts() {
        return postMapper.toResponseList(postRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<PostResponse> getActivePosts() {
        return postMapper.toResponseList(postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE));
    }

    @Override
    public List<PostResponse> getPendingPosts() {
        return postMapper.toResponseList(postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.PENDING));
    }

    @Override
    public List<PostResponse> getHiddenPosts() {
        return postMapper.toResponseList(postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.HIDDEN));
    }

    @Override
    public List<PostResponse> getRejectPosts() {
        return postMapper.toResponseList(postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.REJECTED));
    }

    @Override
    public void deletePost(String id) {
        Post post = findPostById(id);
        if (!post.getLandlordId().equals(securityService.getCurrentUser().getId())) {
            throw new RuntimeException("Không có quyền");
        }
        if (post.getImages() != null) post.getImages().forEach(cloudinaryService::deleteByUrl);
        postRepository.delete(post);
    }

    public Post findPostById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Post"));
    }
}