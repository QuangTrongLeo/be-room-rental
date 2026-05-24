package tmdt.be_room_rental.dto.req.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageNotificationRequest {

    @NotBlank(message = "recipientId không được để trống")
    private String recipientId;

    private String messagePreview;
}
