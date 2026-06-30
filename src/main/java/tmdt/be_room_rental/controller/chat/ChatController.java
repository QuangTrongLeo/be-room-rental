package tmdt.be_room_rental.controller.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.chat.AIChatRequest;
import tmdt.be_room_rental.dto.req.chat.ChatRoomRequest;
import tmdt.be_room_rental.dto.req.chat.MessageNotificationRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.dto.res.chat.ChatRoomResponse;
import tmdt.be_room_rental.enums.type.NotificationType;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.notification.NotificationService;
import tmdt.be_room_rental.service.interfaces.chat.IChatAIService;
import tmdt.be_room_rental.service.interfaces.chat.IChatRoomService;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatRoomService chatRoomService;
    private final IChatAIService chatAIService;
    private final NotificationService notificationService;
    private final SecurityService securityService;

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

    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<List<UserResponse>> getContacts() {
        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách liên hệ chat thành công.")
                .data(chatRoomService.getContacts())
                .build();
    }

    @PostMapping("/ai")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<Object> chatAI(@Valid @RequestBody AIChatRequest request) {
        Object aiResponse = chatAIService.chatAI(request);

        return ApiResponse.<Object>builder()
                .code(HttpStatus.OK.value())
                .message("Trợ lý AI phản hồi thành công.")
                .data(aiResponse)
                .build();
    }

    /**
     * Gửi thông báo tin nhắn mới cho người nhận.
     * FE gọi endpoint này sau khi gửi tin nhắn thành công qua Firebase.
     * <p>
     * Request Body: { "recipientId": "<id người nhận>", "messagePreview": "Nội dung..." }
     */
    @PostMapping("/notify-message")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<Void> notifyNewMessage(@Valid @RequestBody MessageNotificationRequest request) {
        var currentUser = securityService.getCurrentUser();

        // Cắt preview nếu quá dài
        String preview = request.getMessagePreview();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }

        notificationService.sendNotification(
                request.getRecipientId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.NEW_MESSAGE,
                "💬 Tin nhắn mới từ " + currentUser.getUsername(),
                preview != null ? preview : "Bạn có một tin nhắn mới",
                null // refId — không cần cho tin nhắn
        );

        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Đã gửi thông báo tin nhắn mới.")
                .build();
    }
}

