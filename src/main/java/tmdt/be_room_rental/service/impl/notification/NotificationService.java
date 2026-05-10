package tmdt.be_room_rental.service.impl.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tmdt.be_room_rental.dto.res.notification.NotificationResponse;
import tmdt.be_room_rental.entity.Notification;
import tmdt.be_room_rental.enums.type.NotificationType;
import tmdt.be_room_rental.repository.notification.NotificationRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    // Map<userId, SseEmitter> — lưu kết nối SSE của từng user
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // ===================== SSE SUBSCRIBE =====================

    /**
     * FE gọi GET /notifications/subscribe để mở kết nối SSE.
     * Trả về emitter — Spring tự stream event khi có thông báo.
     */
    public SseEmitter subscribe(String userId) {
        // Timeout 30 phút
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.put(userId, emitter);

        // Cleanup khi kết nối đóng
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        // Gửi event connect để FE biết đã kết nối thành công
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("SSE connected for user: " + userId));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        log.info("SSE subscribed for user: {}", userId);
        return emitter;
    }

    // ===================== GỬI THÔNG BÁO =====================

    /**
     * Tạo thông báo, lưu DB và đẩy real-time qua SSE nếu user đang online.
     */
    public void sendNotification(String recipientId, String senderId, String senderName,
            NotificationType type, String title, String message, String refId) {
        // 1. Lưu vào DB
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .senderId(senderId)
                .senderName(senderName)
                .type(type)
                .title(title)
                .message(message)
                .refId(refId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);

        // 2. Push real-time qua SSE nếu recipient đang online
        SseEmitter emitter = emitters.get(recipientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(response));
                log.info("SSE sent to user: {}", recipientId);
            } catch (IOException e) {
                emitters.remove(recipientId);
                log.warn("SSE send failed, removed emitter for user: {}", recipientId);
            }
        }
    }

    // ===================== REST API =====================

    /**
     * Lấy danh sách thông báo của user hiện tại
     */
    public List<NotificationResponse> getMyNotifications() {
        String userId = securityService.getCurrentUser().getId();
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    public long countUnread() {
        String userId = securityService.getCurrentUser().getId();
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    /**
     * Đánh dấu 1 thông báo đã đọc
     */
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Đánh dấu tất cả thông báo của tôi là đã đọc
     */
    public void markAllAsRead() {
        String userId = securityService.getCurrentUser().getId();
        List<Notification> unread = notificationRepository
                .findAllByRecipientIdOrderByCreatedAtDesc(userId)
                .stream().filter(n -> !n.isRead()).collect(Collectors.toList());

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ===================== PRIVATE =====================

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .senderId(n.getSenderId())
                .senderName(n.getSenderName())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .refId(n.getRefId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
