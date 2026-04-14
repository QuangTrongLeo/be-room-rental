package tmdt.be_room_rental.dto.res.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    /**
     * roomId dùng để FE kết nối với Firebase:
     * Firestore: /chat_rooms/{roomId}/messages
     * Realtime DB: chat_rooms/{roomId}/messages
     */
    private String roomId;

    private List<String> participantIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
