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

@Document(collection = "subscriptions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Subscription {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    @Indexed(unique = true)
    private String landlordId;
    private String packageId; // ID của gói đang sử dụng
    private int remainingPosts; // Số lượt còn lại (3 hoặc 5 giảm dần)
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}
