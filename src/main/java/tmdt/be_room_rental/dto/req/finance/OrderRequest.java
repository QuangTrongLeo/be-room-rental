package tmdt.be_room_rental.dto.req.finance;

import lombok.Data;
import tmdt.be_room_rental.enums.status.OrderStatus;

@Data
public class OrderRequest {
    private String packageId;
    private String voucherId;
    private Double totalPrice;
    private OrderStatus status;
}
