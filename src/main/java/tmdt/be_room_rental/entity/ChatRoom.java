package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    /**
     * roomId được tạo theo format: min(id1, id2) + "_" + max(id1, id2)
     * Đảm bảo tính duy nhất, không bị trùng khi đổi chiều chat.
     */
    @Id
    private String id;

    private List<String> participantIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
