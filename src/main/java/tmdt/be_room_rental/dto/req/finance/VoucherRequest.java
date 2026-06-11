package tmdt.be_room_rental.dto.req.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VoucherRequest {
    private Integer quantity;
    private Double discountPercentage;
    private Double maxDiscountAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate startedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate expiredAt;
    private Boolean isActive;
}
