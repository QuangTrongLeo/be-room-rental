package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String roomId; // Trỏ về Room
    private String landlordId;
    private String title;
    private String description;
    private double price;
    private PostStatus status;
    private boolean isBoosted; // Đánh dấu bài này có đang trong gói đẩy tin không
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
