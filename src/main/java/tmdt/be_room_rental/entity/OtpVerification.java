package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "otp_verifications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OtpVerification {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String email;
    private String otp;
    private LocalDateTime expiredAt;
}
