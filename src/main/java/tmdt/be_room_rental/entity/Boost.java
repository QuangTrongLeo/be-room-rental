package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "boosts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Boost {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String postId;
    private String landlordId;
    private double price; // Giá tiền của 1 lần đẩy tin này
    private LocalDateTime startTime;
    private LocalDateTime endTime; // Hết thời gian này bài sẽ không còn nổi bật nữa
    private boolean active;
}
