package tmdt.be_room_rental.dto.res.post;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostHistoryResponse {
    private String id;
    private LocalDateTime viewedAt;
    private PostResponse post;
}
