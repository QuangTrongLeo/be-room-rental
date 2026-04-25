package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.OrderStatus;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.mapper.post.PostMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.auth.UserService;
import tmdt.be_room_rental.service.impl.finance.OrderService;
import tmdt.be_room_rental.service.impl.finance.PackageService;
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
    private final UserRepository userRepository;
    private final IPostHistoryService postHistoryService;
    private final ICloudinaryService cloudinaryService;
    private final UserService userService;
    private final PackageService packageService;
    private final OrderService orderService;
    private final SecurityService securityService;
    private final TaskScheduler taskScheduler;
    private final PostMapper postMapper;

    private static final int MAX_IMAGES = 8;

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();

        if (currentUser.getPostQuota() <= 0) {
            throw new RuntimeException("Bạn đã hết lượt đăng bài. Vui lòng mua thêm gói.");
        }

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
    @Transactional
    public PostResponse updateStatusPost(String id, PostRequest request) {
        User currentUser = securityService.getCurrentUser();
        Post post = findPostById(id);
        PostStatus newStatus = request.getStatus();
        PostStatus oldStatus = post.getStatus();

        boolean isAdmin = currentUser.getRole().equals(RoleEnum.ADMIN);
        boolean isLandlord = currentUser.getRole().equals(RoleEnum.LANDLORD);
        boolean isOwner = post.getLandlordId().equals(currentUser.getId());

        if (isAdmin) {
            // ADMIN: Có quyền chuyển sang bất cứ trạng thái nào
            // Nếu Admin duyệt bài: PENDING -> ACTIVE
            if (oldStatus == PostStatus.PENDING && newStatus == PostStatus.ACTIVE) {
                executeApprovalPost(post);
            }
        } else if (isLandlord && isOwner) {
            // LANDLORD: Chỉ được phép các luồng cụ thể
            validateLandlordStatusTransition(oldStatus, newStatus, post);
        } else {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này.");
        }

        post.setStatus(newStatus);
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
    public List<PostResponse> getPostsByAmenityId(String amenityId) {
        List<Post> posts = postRepository.findAllByAmenitiesContainingAndStatusOrderByCreatedAtDesc(amenityId, PostStatus.ACTIVE);
        return postMapper.toResponseList(posts);
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

    /**
     * Logic kiểm tra quyền chuyển đổi trạng thái dành cho Chủ trọ
     */
    private void validateLandlordStatusTransition(PostStatus oldStatus, PostStatus newStatus, Post post) {
        // 1. EXPIRED -> PENDING: Đăng ký duyệt lại khi hết hạn
        if (oldStatus == PostStatus.EXPIRED && newStatus == PostStatus.PENDING) return;

        // 2. ACTIVE -> HIDDEN: Chủ trọ muốn tạm ẩn bài đang đăng
        if (oldStatus == PostStatus.ACTIVE && newStatus == PostStatus.HIDDEN) return;

        // 3. HIDDEN -> ACTIVE: Hiện lại bài (Chỉ khi đã từng được duyệt - có approvedAt)
        if (oldStatus == PostStatus.HIDDEN && newStatus == PostStatus.ACTIVE) {
            if (post.getApprovedAt() == null) {
                throw new RuntimeException("Bài đăng chưa được duyệt nên không thể tự hiển thị.");
            }
            // Kiểm tra xem bài có còn trong hạn dùng không
            if (post.getExpiredAt() != null && post.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Bài đăng đã hết hạn, vui lòng chuyển về Chờ duyệt để gia hạn.");
            }
            return;
        }

        throw new RuntimeException("Chủ trọ không được phép chuyển trạng thái từ " + oldStatus + " sang " + newStatus);
    }

    /**
     * Logic nghiệp vụ khi bài đăng được chuyển sang ACTIVE (Trừ Quota, Tính ngày, Lập lịch)
     */
    private void executeApprovalPost(Post post) {
        User landlord = userService.findUserById(post.getLandlordId());

        if (landlord.getPostQuota() <= 0) {
            throw new RuntimeException("Chủ trọ đã hết lượt đăng bài.");
        }

        // Lấy gói dịch vụ thành công mới nhất
        Order latestOrder = orderService.findOrderOfLandlord(landlord.getId(), OrderStatus.SUCCESS);
        Packages pkg = packageService.findPackageById(latestOrder.getPackageId());

        // Trừ lượt đăng
        landlord.setPostQuota(landlord.getPostQuota() - 1);
        userRepository.save(landlord);

        // Thiết lập thời gian
        LocalDateTime now = LocalDateTime.now();
        post.setApprovedAt(now);
        LocalDateTime expiredAt = now.plusDays(pkg.getActiveDays());
        post.setExpiredAt(expiredAt);

        // Lập lịch ẩn bài
        taskScheduler.schedule(() -> {
            handlePostExpiration(post.getId());
        }, java.sql.Timestamp.valueOf(expiredAt).toInstant());
    }

    private void handlePostExpiration(String postId) {
        postRepository.findById(postId).ifPresent(post -> {
            if (post.getStatus() == PostStatus.ACTIVE) {
                post.setStatus(PostStatus.EXPIRED);
                postRepository.save(post);
            }
        });
    }
}