package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "amenities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name; // Ví dụ: Máy lạnh, Wifi, Tủ lạnh
}
