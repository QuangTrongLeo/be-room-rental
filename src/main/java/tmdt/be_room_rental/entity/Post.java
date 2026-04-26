package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.status.PostStatus;
import tmdt.be_room_rental.enums.type.PackageTier;
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
    private String address;

    @Builder.Default
    private Integer views = 0;
    @Builder.Default
    private Integer favorites = 0;

    private Double price;
    private Double area;

    private List<String> amenities;
    private List<String> images;

    private RoomType roomType;
    private PackageTier postingTier;
    private PackageTier boostingTier;
    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    @GeoSpatialIndexed
    private GeoJsonPoint location;

    // --- Thời gian ---
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime boostExpiredAt;
    private LocalDateTime expiredAt;
}