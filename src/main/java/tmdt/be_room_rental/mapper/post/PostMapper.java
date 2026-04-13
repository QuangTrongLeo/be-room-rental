package tmdt.be_room_rental.mapper.post;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.PostResponse;
import tmdt.be_room_rental.entity.Post;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {
    public PostResponse toResponse(Post post) {
        if (post == null) return null;
        return PostResponse.builder()
                .id(post.getId())
                .roomId(post.getRoomId())
                .landlordId(post.getLandlordId())
                .title(post.getTitle())
                .content(post.getContent())
                .price(post.getPrice())
                .status(post.getStatus())
                .isBoosted(post.isBoosted())
                .views(post.getViews())
                .favorites(post.getFavorites())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public List<PostResponse> toResponseList(List<Post> posts) {
        return posts.stream().map(this::toResponse).collect(Collectors.toList());
    }
}