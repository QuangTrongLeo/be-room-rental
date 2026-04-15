package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "post_history") // Lịch sử xem bài đăng
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostHistory {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed
    private String userId;
    @Indexed
    private String postId;

    private LocalDateTime viewedAt;
}