package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.PackageType;

import java.util.UUID;

@Document(collection = "packages")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Package {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name; // "Gói Bình Thường", "Gói Premium"
    private PackageType type;
    private int maxPosts;    // 3 hoặc 5 bài
    private int durationDays; // 14 hoặc 30 ngày
    private double price;
}
