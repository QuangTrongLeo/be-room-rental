package tmdt.be_room_rental.dto.req.finance;

import lombok.Data;
import tmdt.be_room_rental.enums.type.VoucherType;

@Data
public class VoucherRequest {
    private String code;
    private Double discountPercentage;
    private Double maxDiscountAmount;
    private Integer quantity;
    private VoucherType type; // WEEK hoặc MONTH
}
