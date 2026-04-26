package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

import java.util.UUID;

@Document(collection = "inventories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed
    private String userId;

    private PackageType type;    // POSTING hoặc BOOSTING
    private PackageTier tier;    // NORMAL hoặc PRO

    private Integer balance = 0;
}