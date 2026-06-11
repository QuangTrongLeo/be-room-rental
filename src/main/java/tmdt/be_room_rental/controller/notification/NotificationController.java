package tmdt.be_room_rental.controller.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.notification.NotificationResponse;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.notification.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityService securityService;

    /**
     * FE subscribe SSE để nhận thông báo real-time.
     * GET /notifications/subscribe
     *
     * Cách dùng ở React:
     * const es = new EventSource('/notifications/subscribe?userId=xxx', {
     * withCredentials: true });
     * es.addEventListener('NOTIFICATION', (e) => { ... });
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        String userId = securityService.getCurrentUser().getId();
        return notificationService.subscribe(userId);
    }

    /**
     * Lấy toàn bộ thông báo của user hiện tại (để hiển thị danh sách)
     * GET /notifications
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .message("Lấy danh sách thông báo thành công.")
                .data(notificationService.getMyNotifications())
                .build();
    }

    /**
     * Lấy số thông báo chưa đọc (dùng cho badge icon)
     * GET /notifications/unread-count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> countUnread() {
        return ApiResponse.<Long>builder()
                .code(200)
                .data(notificationService.countUnread())
                .build();
    }

    /**
     * Đánh dấu 1 thông báo đã đọc
     * PUT /notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã đánh dấu đọc.")
                .build();
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     * PUT /notifications/read-all
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã đánh dấu tất cả là đã đọc.")
                .build();
    }
}
