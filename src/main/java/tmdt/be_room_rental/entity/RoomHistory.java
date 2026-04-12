package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "room_history") // Lịch sử xem phòng
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomHistory {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String userId;
    private String postId; // Lưu PostId để biết lúc đó họ xem tin nào
    private LocalDateTime viewedAt;
}
