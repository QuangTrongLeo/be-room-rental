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
import tmdt.be_room_rental.enums.type.PackageType;
import tmdt.be_room_rental.mapper.post.PostMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.auth.UserService;
import tmdt.be_room_rental.service.impl.finance.InventoryService;
import tmdt.be_room_rental.service.impl.finance.OrderService;
import tmdt.be_room_rental.service.impl.finance.PackageService;
import tmdt.be_room_rental.service.interfaces.auth.ICloudinaryService;
import tmdt.be_room_rental.service.interfaces.post.IPostHistoryService;
import tmdt.be_room_rental.service.interfaces.post.IPostService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final PackageService packageService;
    private final InventoryService inventoryService;
    private final IPostHistoryService postHistoryService;
    private final ICloudinaryService cloudinaryService;
    private final SecurityService securityService;
    private final TaskScheduler taskScheduler;
    private final PostMapper postMapper;
    private static final int MAX_IMAGES = 8;

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();
        inventoryService.checkInventoryAvailability(currentUser.getId(), PackageType.POSTING, request.getPostingTier());
        // Kiểm tra lượt Boost (Nếu người dùng có yêu cầu Boost)
        if (request.getBoostingTier() != null) {
            inventoryService.checkInventoryAvailability(currentUser.getId(), PackageType.BOOSTING, request.getBoostingTier());
        }

        // Kiểm tra ảnh
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new RuntimeException("Chỉ được upload tối đa " + MAX_IMAGES + " ảnh");
        }

        Post post = buildPost(currentUser, request);

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

        if (!post.getLandlordId().equals(currentUser.getId())) throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getPrice() != null) post.setPrice(request.getPrice());
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
    public PostResponse approveActivePost(String id) {
        Post post = findPostById(id);
        if (post.getStatus() != PostStatus.PENDING) {
            throw new RuntimeException("Bài đăng không ở trạng thái chờ duyệt.");
        }
        LocalDateTime now = LocalDateTime.now();

        // --- Xử lý Đăng bài (Posting) ---
        Packages pkgPosting = packageService.findPackageByTypeAndTier(PackageType.POSTING, post.getPostingTier());
        inventoryService.consumeInventory(post.getLandlordId(), PackageType.POSTING, post.getPostingTier());
        post.setStatus(PostStatus.ACTIVE);
        post.setApprovedAt(now);
        LocalDateTime expiryDate = now.plusDays(pkgPosting.getActiveDays());
        post.setExpiredAt(expiryDate);

        // Lập lịch tự động chuyển sang EXPIRED
        taskScheduler.schedule(() -> {
            handlePostExpired(id);
        }, expiryDate.atZone(ZoneId.systemDefault()).toInstant());

        // --- Xử lý Đẩy bài (Boosting) - Nếu có ---
        if (post.getBoostingTier() != null) {
            Packages pkgBoosting = packageService.findPackageByTypeAndTier(PackageType.BOOSTING, post.getBoostingTier());
            inventoryService.consumeInventory(post.getLandlordId(), PackageType.BOOSTING, post.getBoostingTier());
            LocalDateTime boostExpiryDate = now.plusDays(pkgBoosting.getActiveDays());
            post.setBoostExpiredAt(boostExpiryDate);
            // Lập lịch tự động gỡ Boost
            taskScheduler.schedule(() -> {
                handleBoostTimeout(id);
            }, boostExpiryDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse rejectActivePost(String id) {
        Post post = findPostById(id);
        if (post.getStatus() != PostStatus.PENDING) throw new RuntimeException("Bài đăng không ở trạng thái chờ duyệt.");
        post.setStatus(PostStatus.REJECTED);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse republishPost(String id) {
        User currentUser = securityService.getCurrentUser();
        Post post = findPostById(id);

        if (!post.getLandlordId().equals(currentUser.getId())) throw new RuntimeException("Bạn không có quyền đăng lại bài này.");

        inventoryService.checkInventoryAvailability(currentUser.getId(), PackageType.POSTING, post.getPostingTier());

        if (post.getBoostingTier() != null) {
            inventoryService.checkInventoryAvailability(currentUser.getId(), PackageType.BOOSTING, post.getBoostingTier());
        }

        post.setStatus(PostStatus.PENDING);
        post.setCreatedAt(LocalDateTime.now());
        post.setApprovedAt(null);
        post.setExpiredAt(null);
        post.setBoostExpiredAt(null);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse getPostById(String id) {
        Post post = findPostById(id);
        post.setViews(post.getViews() + 1);
        String currentUserId = securityService.getCurrentUser().getId();
        if (currentUserId != null) {
            postHistoryService.saveHistory(currentUserId, id);
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
        if (!post.getLandlordId().equals(securityService.getCurrentUser().getId())) throw new RuntimeException("Không có quyền");
        if (post.getImages() != null) post.getImages().forEach(cloudinaryService::deleteByUrl);
        postRepository.delete(post);
    }

    public Post findPostById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Post"));
    }

    private Post buildPost(User user, PostRequest request) {
        Post post = Post.builder()
                .landlordId(user.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .area(request.getArea())
                .address(request.getAddress())
                .amenities(request.getAmenities())
                .roomType(request.getRoomType())
                .postingTier(request.getPostingTier())
                .boostingTier(request.getBoostingTier())
                .status(PostStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .views(0)
                .favorites(0)
                .build();

        // Xử lý tọa độ địa lý
        if (request.getLongitude() != null && request.getLatitude() != null) {
            post.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        return post;
    }

    private void handlePostExpired(String postId) {
        try {
            postRepository.findById(postId).ifPresent(post -> {
                if (post.getStatus() == PostStatus.ACTIVE &&
                        post.getExpiredAt() != null &&
                        post.getExpiredAt().isBefore(LocalDateTime.now().plusSeconds(1))) {
                    post.setStatus(PostStatus.EXPIRED);
                    post.setBoostExpiredAt(null);
                    postRepository.save(post);
                }
            });
        } catch (Exception e) {
            System.err.println("LOG: Lỗi xử lý hết hạn bài đăng: " + e.getMessage());
        }
    }

    private void handleBoostTimeout(String postId) {
        try {
            postRepository.findById(postId).ifPresent(post -> {
                if (post.getBoostExpiredAt() != null &&
                        post.getBoostExpiredAt().isBefore(LocalDateTime.now().plusSeconds(1))) {
                    post.setBoostExpiredAt(null);
                    postRepository.save(post);
                }
            });
        } catch (Exception e) {
            System.err.println("LOG: Lỗi xử lý hết hạn Boost: " + e.getMessage());
        }
    }
}