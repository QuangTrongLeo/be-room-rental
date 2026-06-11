package tmdt.be_room_rental.controller.post;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.post.BookingRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.post.BookingResponse;
import tmdt.be_room_rental.service.interfaces.post.IBookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    // ===================== TENANT =====================

    /**
     * Tenant tạo lịch hẹn xem phòng
     * POST /bookings
     * Body: { "postId": "...", "bookingTime": "2025-06-01T10:00:00" }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Đặt lịch hẹn xem phòng thành công.")
                .data(bookingService.createBooking(request))
                .build();
    }

    /**
     * Tenant xem danh sách lịch hẹn của mình
     * GET /bookings/my-bookings
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<List<BookingResponse>> getMyBookings() {
        return ApiResponse.<List<BookingResponse>>builder()
                .code(200)
                .message("Lấy danh sách lịch hẹn của bạn thành công.")
                .data(bookingService.getMyBookings())
                .build();
    }

    /**
     * Tenant hủy lịch hẹn (chỉ khi PENDING)
     * PUT /bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable String id) {
        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Đã hủy lịch hẹn thành công.")
                .data(bookingService.cancelBooking(id))
                .build();
    }

    // ===================== LANDLORD =====================

    /**
     * Landlord xem tất cả booking gửi đến phòng của mình
     * GET /bookings/landlord
     */
    @GetMapping("/landlord")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ApiResponse<List<BookingResponse>> getBookingsForLandlord() {
        return ApiResponse.<List<BookingResponse>>builder()
                .code(200)
                .message("Lấy danh sách lịch hẹn thành công.")
                .data(bookingService.getBookingsForLandlord())
                .build();
    }

    /**
     * Landlord duyệt lịch hẹn
     * PUT /bookings/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ApiResponse<BookingResponse> approveBooking(@PathVariable String id) {
        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Đã duyệt lịch hẹn thành công.")
                .data(bookingService.approveBooking(id))
                .build();
    }

    /**
     * Landlord từ chối lịch hẹn
     * PUT /bookings/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ApiResponse<BookingResponse> rejectBooking(@PathVariable String id) {
        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Đã từ chối lịch hẹn.")
                .data(bookingService.rejectBooking(id))
                .build();
    }

    // ===================== CHUNG =====================

    /**
     * Xem chi tiết 1 booking (chỉ tenant hoặc landlord của booking đó)
     * GET /bookings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable String id) {
        return ApiResponse.<BookingResponse>builder()
                .code(200)
                .message("Lấy chi tiết lịch hẹn thành công.")
                .data(bookingService.getBookingById(id))
                .build();
    }
}
