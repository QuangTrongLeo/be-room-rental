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
    private String roomId;
    private String landlordId;
    private String title;
    private String content;
    private double price;
    private PostStatus status = PostStatus.PENDING;
    private boolean isBoosted = false;
    private int views = 0;
    private int favorites = 0;
    private LocalDateTime createdAt;
}
