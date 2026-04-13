package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.RoomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "rooms")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Room {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String landlordId;
    private String name;
    private String address;
    private String description;
    private double area; // Diện tích
    private double price;
    private List<String> amenities;
    private List<String> images;
    private RoomType roomType;

    @GeoSpatialIndexed
    private GeoJsonPoint location;
    private LocalDateTime createdAt;
}
