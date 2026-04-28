package tmdt.be_room_rental.dto.res.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private String id;
    private String userId;
    private String postId;
    private PostResponse post;
    private LocalDateTime createdAt;
}
