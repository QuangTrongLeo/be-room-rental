package tmdt.be_room_rental.dto.req.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRoomRequest {

    @NotBlank(message = "targetUserId không được để trống")
    private String targetUserId;
}
