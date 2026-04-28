package tmdt.be_room_rental.mapper.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.FavoriteResponse;
import tmdt.be_room_rental.entity.Favorite;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.repository.post.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FavoriteMapper {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public FavoriteResponse toResponse(Favorite favorite) {
        if (favorite == null) return null;

        // Fetch the corresponding Post and map it to PostResponse
        Post post = postRepository.findById(favorite.getPostId()).orElse(null);

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .postId(favorite.getPostId())
                .post(post != null ? postMapper.toResponse(post) : null)
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    public List<FavoriteResponse> toResponseList(List<Favorite> favorites) {
        if (favorites == null) return List.of();
        return favorites.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
