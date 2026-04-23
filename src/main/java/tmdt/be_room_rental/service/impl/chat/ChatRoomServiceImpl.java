package tmdt.be_room_rental.service.impl.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.chat.ChatRoomResponse;
import tmdt.be_room_rental.entity.ChatRoom;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.repository.chat.ChatRoomRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.chat.IChatRoomService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements IChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final SecurityService securityService;

    @Override
    public ChatRoomResponse getOrCreateRoom(String targetUserId) {
        User currentUser = securityService.getCurrentUser();
        String currentUserId = currentUser.getId();

        // Sắp xếp theo alphabet để đảm bảo roomId luôn là duy nhất
        // cho 2 user bất kể ai gọi trước
        String roomId = buildRoomId(currentUserId, targetUserId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseGet(() -> createAndSaveRoom(roomId, currentUserId, targetUserId));

        return mapToResponse(chatRoom);
    }

    // ==================== Private Helpers ====================

    /**
     * Tạo roomId chuẩn hóa từ 2 userId.
     * Sắp xếp theo alphabet: min_max -> đảm bảo A chat với B hay B chat với A
     * đều ra cùng 1 roomId.
     */
    private String buildRoomId(String id1, String id2) {
        return id1.compareTo(id2) < 0
                ? id1 + "_" + id2
                : id2 + "_" + id1;
    }

    private ChatRoom createAndSaveRoom(String roomId, String currentUserId, String targetUserId) {
        ChatRoom newRoom = ChatRoom.builder()
                .id(roomId)
                .participantIds(List.of(currentUserId, targetUserId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return chatRoomRepository.save(newRoom);
    }

    private ChatRoomResponse mapToResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .participantIds(chatRoom.getParticipantIds())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}
