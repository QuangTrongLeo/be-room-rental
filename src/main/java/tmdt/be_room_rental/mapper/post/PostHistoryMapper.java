package tmdt.be_room_rental.mapper.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.post.PostHistoryResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.PostHistory;
import tmdt.be_room_rental.repository.post.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostHistoryMapper {

    private final PostMapper postMapper;
    private final PostRepository postRepository;

    public PostHistoryResponse toResponse(PostHistory history) {
        if (history == null) return null;

        // Lấy thông tin bài post tương ứng
        Post post = postRepository.findById(history.getPostId()).orElse(null);

        return PostHistoryResponse.builder()
                .id(history.getId())
                .viewedAt(history.getViewedAt())
                .post(postMapper.toResponse(post))
                .build();
    }

    public List<PostHistoryResponse> toResponseList(List<PostHistory> histories) {
        return histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}