package tmdt.be_room_rental.dto.res.finance;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.status.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private String packageId;
    private String voucherId;
    private String vnpTxnRef;
    private Double totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
