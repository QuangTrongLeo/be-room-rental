package tmdt.be_room_rental.dto.res.finance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private String id;
    private Double discountPercentage;
    private Double maxDiscountAmount;
    private Integer quantity;
    private Integer usedCount;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
    private Boolean isActive;
}
