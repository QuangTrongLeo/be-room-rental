package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.status.TransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "transactions") // Lịch sử mua hàng
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String landlordId;
    private String targetId;
    private double amount;
    private String paymentMethod; // VNPAY, MOMO
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
