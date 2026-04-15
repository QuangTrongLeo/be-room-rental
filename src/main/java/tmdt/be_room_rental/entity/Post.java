package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.enums.type.RoomType;

import java.time.LocalDateTime;
import java.util.List;
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

    @Indexed
    private String landlordId;
    private String title;
    private String content;

    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    @Builder.Default
    private Boolean isBoosted = false;

    @Builder.Default
    private Integer views = 0;
    @Builder.Default
    private Integer favorites = 0;

    // --- Thông tin đặc trưng của phòng ---
    private Double price;
    private Double area;
    private String address;
    private List<String> amenities;
    private List<String> images;
    private RoomType roomType;

    @GeoSpatialIndexed
    private GeoJsonPoint location;

    // --- Thời gian ---
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredAt;
}