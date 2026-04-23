package tmdt.be_room_rental.controller.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.chat.ChatRoomRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.chat.ChatRoomResponse;
import tmdt.be_room_rental.service.interfaces.chat.IChatRoomService;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatRoomService chatRoomService;

    /**
     * Lấy hoặc tạo phòng chat giữa user hiện tại và targetUserId.
     * <p>
     * Cả USER lẫn LANDLORD đều có thể gọi endpoint này.
     * Backend tự xác định "người gọi" qua JWT token, không cần truyền thêm.
     * <p>
     * Request Body: { "targetUserId": "<id của đối phương>" }
     * Response: { roomId, participantIds, createdAt, updatedAt }
     * <p>
     * FE dùng roomId để kết nối Firebase:
     * - Firestore : /chat_rooms/{roomId}/messages
     * - Realtime DB: chat_rooms/{roomId}/messages
     */
    @PostMapping("/room")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<ChatRoomResponse> getOrCreateChatRoom(
            @Valid @RequestBody ChatRoomRequest request) {

        ChatRoomResponse response = chatRoomService.getOrCreateRoom(request.getTargetUserId());

        return ApiResponse.<ChatRoomResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy phòng chat thành công.")
                .data(response)
                .build();
    }
}
