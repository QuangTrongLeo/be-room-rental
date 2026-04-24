package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String userId;
    private String packageId;
    private String voucherId;

    private Double totalPrice;

    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    private String vnpTxnRef;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}