package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.res.post.FavoriteResponse;
import tmdt.be_room_rental.entity.Favorite;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.mapper.post.FavoriteMapper;
import tmdt.be_room_rental.repository.post.FavoriteRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.post.IFavoriteService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(String postId) {
        User currentUser = securityService.getCurrentUser();

        // 1. Kiểm tra xem bài đăng có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        // 2. Kiểm tra xem user đã yêu thích bài này chưa
        if (favoriteRepository.existsByUserIdAndPostId(currentUser.getId(), postId)) {
            throw new RuntimeException("Bạn đã thêm bài đăng này vào danh sách yêu thích rồi");
        }

        // 3. Tạo record Favorite mới
        Favorite favorite = Favorite.builder()
                .userId(currentUser.getId())
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();
        favorite = favoriteRepository.save(favorite);

        // 4. Tăng số đếm favorites của bài đăng lên 1
        post.setFavorites(post.getFavorites() + 1);
        postRepository.save(post);

        return favoriteMapper.toResponse(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(String postId) {
        User currentUser = securityService.getCurrentUser();

        // 1. Kiểm tra xem record yêu thích có tồn tại không
        Favorite favorite = favoriteRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng chưa có trong danh sách yêu thích"));

        // 2. Xóa record Favorite
        favoriteRepository.delete(favorite);

        // 3. Giảm số đếm favorites của bài đăng xuống 1
        Post post = postRepository.findById(postId).orElse(null);
        if (post != null) {
            post.setFavorites(Math.max(0, post.getFavorites() - 1));
            postRepository.save(post);
        }
    }

    @Override
    public List<FavoriteResponse> getMyFavorites() {
        User currentUser = securityService.getCurrentUser();
        
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return favoriteMapper.toResponseList(favorites);
    }

    @Override
    public boolean isFavorited(String postId) {
        try {
            User currentUser = securityService.getCurrentUser();
            return favoriteRepository.existsByUserIdAndPostId(currentUser.getId(), postId);
        } catch (Exception e) {
            return false; // Nếu chưa đăng nhập thì mặc định là false
        }
    }
}
