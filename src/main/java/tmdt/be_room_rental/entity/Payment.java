package tmdt.be_room_rental.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "payments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String transactionId; // Link tới Entity Transaction
    private String vnp_TxnRef;    // Mã tham chiếu giao dịch gửi sang VNPAY
    private String vnp_TransactionNo; // Mã giao dịch ghi nhận tại hệ thống VNPAY
    private String vnp_OrderInfo;
    private String vnp_ResponseCode; // 00 là thành công
    private String vnp_BankCode;     // Mã ngân hàng thanh toán

    private double amount;
    private LocalDateTime payDate;
}