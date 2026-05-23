package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.req.post.PostRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;
import tmdt.be_room_rental.mapper.enums.PostEnumMapper;
import tmdt.be_room_rental.mapper.post.PostMapper;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.finance.InventoryService;
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
    private final LocationService locationService;
    private final ICloudinaryService cloudinaryService;
    private final SecurityService securityService;
    private final TaskScheduler taskScheduler;
    private final PostEnumMapper postEnumMapper;
    private final PostMapper postMapper;
    private static final int MAX_IMAGES = 8;

    @Override
    public List<EnumResponse> getPostsStatus() {
        return postEnumMapper.toStatusResponseList();
    }

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        User currentUser = securityService.getCurrentUser();

        checkAndConsumePostInventory(currentUser.getId(), request.getPostingTier(), request.getBoostingTier());

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
    public PostResponse approvePost(String id) {
        Post post = findPostById(id);
        if (post.getStatus() != PostStatus.PENDING) {
            throw new RuntimeException("Bài đăng không ở trạng thái chờ duyệt.");
        }
        LocalDateTime now = LocalDateTime.now();

        Packages pkgPosting = packageService.findPackageByTypeAndTier(PackageType.POSTING, post.getPostingTier());

        post.setStatus(PostStatus.ACTIVE);
        post.setApprovedAt(now);
        LocalDateTime expiryDate = now.plusDays(pkgPosting.getActiveDays());
        post.setExpiredAt(expiryDate);

        taskScheduler.schedule(() -> handlePostExpired(id), expiryDate.atZone(ZoneId.systemDefault()).toInstant());

        if (post.getBoostingTier() != null) {
            Packages pkgBoosting = packageService.findPackageByTypeAndTier(PackageType.BOOSTING, post.getBoostingTier());
            LocalDateTime boostExpiryDate = now.plusDays(pkgBoosting.getActiveDays());
            post.setBoostExpiredAt(boostExpiryDate);

            taskScheduler.schedule(() -> handleBoostTimeout(id), boostExpiryDate.atZone(ZoneId.systemDefault()).toInstant());
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse rejectPost(String id) {
        Post post = findPostById(id);
        if (post.getStatus() != PostStatus.PENDING) {
            throw new RuntimeException("Bài đăng không ở trạng thái chờ duyệt.");
        }

        post.setStatus(PostStatus.REJECTED);
        inventoryService.refundInventory(post.getLandlordId(), PackageType.POSTING, post.getPostingTier());

        if (post.getBoostingTier() != null) {
            inventoryService.refundInventory(post.getLandlordId(), PackageType.BOOSTING, post.getBoostingTier());
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostResponse republishPost(String id) {
        User currentUser = securityService.getCurrentUser();
        Post post = findPostById(id);

        if (!post.getLandlordId().equals(currentUser.getId())) throw new RuntimeException("Bạn không có quyền đăng lại bài này.");

        checkAndConsumePostInventory(currentUser.getId(), post.getPostingTier(), post.getBoostingTier());

        post.setStatus(PostStatus.PENDING);
        post.setCreatedAt(LocalDateTime.now());
        post.setApprovedAt(null);
        post.setExpiredAt(null);
        post.setBoostExpiredAt(null);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse toggleActiveHiddenPost(String id) {
        User currentUser = securityService.getCurrentUser();
        Post post = findPostById(id);
        PostStatus currentStatus = post.getStatus();

        boolean isAdmin = currentUser.getRole().equals(RoleEnum.ADMIN);
        boolean isOwner = post.getLandlordId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng của người khác.");
        if (currentStatus == PostStatus.ACTIVE) {
            post.setStatus(PostStatus.HIDDEN);
        }
        else if (currentStatus == PostStatus.HIDDEN) {
            if (post.getApprovedAt() == null) throw new RuntimeException("Bài đăng chưa được duyệt, không thể hiển thị.");
            if (post.getExpiredAt() != null && post.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Bài đăng đã hết hạn. Hãy thực hiện 'Đăng lại' để gia hạn.");
            }
            post.setStatus(PostStatus.ACTIVE);
        }

        return postMapper.toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse getPostById(String id) {
        Post post = findPostById(id);
        post.setViews(post.getViews() + 1);

        User currentUser = securityService.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null) {
            postHistoryService.saveHistory(currentUser.getId(), id);
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
        return postMapper.toResponseList(postRepository.findAllByAmenitiesContainingAndStatusOrderByCreatedAtDesc(amenityId, PostStatus.ACTIVE));
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
    public List<PostResponse> getPostsByProvince(String province) {
        List<Double[]> coords = locationService.getProvincePolygon(province);
        if (coords == null || coords.isEmpty()) return List.of();

        List<Point> points = coords.stream().map(c -> new Point(c[0], c[1])).toList();
        GeoJsonPolygon provinceArea = new GeoJsonPolygon(points);

        return postMapper.toResponseList(postRepository.findByLocationWithinAndStatus(provinceArea, PostStatus.ACTIVE));
    }

    @Override
    @Transactional
    public void deletePost(String id) {
        Post post = findPostById(id);
        if (!post.getLandlordId().equals(securityService.getCurrentUser().getId())) throw new RuntimeException("Không có quyền");

        if (post.getStatus() == PostStatus.PENDING) {
            inventoryService.refundInventory(post.getLandlordId(), PackageType.POSTING, post.getPostingTier());
            if (post.getBoostingTier() != null) {
                inventoryService.refundInventory(post.getLandlordId(), PackageType.BOOSTING, post.getBoostingTier());
            }
        }

        if (post.getImages() != null) post.getImages().forEach(cloudinaryService::deleteByUrl);
        postRepository.delete(post);
    }

    public Post findPostById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Post"));
    }

    /**
     * Helper 1: Giải quyết triệt để cảnh báo trùng lặp code khi kiểm tra và trừ lượt gói tin
     */
    private void checkAndConsumePostInventory(String userId, PackageTier postingTier, PackageTier boostingTier) {
        inventoryService.checkInventoryAvailability(userId, PackageType.POSTING, postingTier);
        inventoryService.consumeInventory(userId, PackageType.POSTING, postingTier);

        if (boostingTier != null) {
            inventoryService.checkInventoryAvailability(userId, PackageType.BOOSTING, boostingTier);
            inventoryService.consumeInventory(userId, PackageType.BOOSTING, boostingTier);
        }
    }

    /**
     * Helper 2: Gộp chung logic lập lịch tự động để dọn sạch lỗi duplicated của IDE
     */
    private void updatePostTimeoutCondition(String postId, java.util.function.Predicate<Post> condition, java.util.function.Consumer<Post> action) {
        try {
            postRepository.findById(postId).ifPresent(post -> {
                if (condition.test(post)) {
                    action.accept(post);
                    postRepository.save(post);
                }
            });
        } catch (Exception e) {
            System.err.println("LOG: Lỗi xử lý tác vụ lập lịch tự động: " + e.getMessage());
        }
    }

    private void handlePostExpired(String postId) {
        updatePostTimeoutCondition(postId,
                post -> post.getStatus() == PostStatus.ACTIVE && post.getExpiredAt() != null && post.getExpiredAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                post -> {
                    post.setStatus(PostStatus.EXPIRED);
                    post.setBoostExpiredAt(null);
                }
        );
    }

    private void handleBoostTimeout(String postId) {
        updatePostTimeoutCondition(postId,
                post -> post.getBoostExpiredAt() != null && post.getBoostExpiredAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                post -> post.setBoostExpiredAt(null)
        );
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

        if (request.getLongitude() != null && request.getLatitude() != null) {
            post.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }
        return post;
    }
}