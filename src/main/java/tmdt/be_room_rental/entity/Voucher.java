package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.type.VoucherType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "vouchers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Voucher {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String code;

    private Double discountPercentage;
    private Double maxDiscountAmount;
    private Integer quantity;

    @Builder.Default
    private Integer usedCount = 0;

    private VoucherType type;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @Builder.Default
    private List<String> usedByUsers = new java.util.ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;
}