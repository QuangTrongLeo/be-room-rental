package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.post.BookingRequest;
import tmdt.be_room_rental.dto.res.post.BookingResponse;
import tmdt.be_room_rental.entity.Booking;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.status.BookingStatus;
import tmdt.be_room_rental.enums.type.NotificationType;
import tmdt.be_room_rental.mapper.post.BookingMapper;
import tmdt.be_room_rental.repository.post.BookingRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.notification.NotificationService;
import tmdt.be_room_rental.service.interfaces.post.IBookingService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    // ===================== TENANT =====================

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User currentUser = securityService.getCurrentUser();

        // 1. Kiểm tra bài đăng tồn tại
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng với ID: " + request.getPostId()));

        // 2. Không cho tự đặt phòng của mình
        if (post.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không thể đặt lịch xem phòng của chính mình.");
        }

        // 3. Kiểm tra đã có booking PENDING cho bài này chưa
        if (bookingRepository.existsByUserIdAndPostIdAndStatus(
                currentUser.getId(), request.getPostId(), BookingStatus.PENDING)) {
            throw new RuntimeException("Bạn đã có lịch hẹn đang chờ xác nhận cho bài đăng này.");
        }

        // 4. Tạo booking
        Booking booking = Booking.builder()
                .userId(currentUser.getId())
                .landlordId(post.getLandlordId())
                .postId(request.getPostId())
                .bookingTime(request.getBookingTime())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);

        // 5. Gửi thông báo cho Landlord
        notificationService.sendNotification(
                post.getLandlordId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.BOOKING_CREATED,
                "Có lịch hẹn mới!",
                currentUser.getUsername() + " muốn đặt lịch xem phòng \"" + post.getTitle() + "\".",
                saved.getId()
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    public List<BookingResponse> getMyBookings() {
        User currentUser = securityService.getCurrentUser();
        return bookingMapper.toResponseList(
                bookingRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId())
        );
    }

    @Override
    public BookingResponse cancelBooking(String bookingId) {
        User currentUser = securityService.getCurrentUser();
        Booking booking = findBookingById(bookingId);

        if (!booking.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy lịch hẹn này.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy lịch hẹn đang ở trạng thái chờ xác nhận.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Gửi thông báo cho Landlord
        Post post = postRepository.findById(booking.getPostId()).orElse(null);
        String postTitle = post != null ? post.getTitle() : "phòng trọ";
        notificationService.sendNotification(
                booking.getLandlordId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.BOOKING_CANCELLED,
                "Lịch hẹn bị hủy",
                currentUser.getUsername() + " đã hủy lịch hẹn xem \"" + postTitle + "\".",
                bookingId
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse confirmRented(String bookingId) {
        User currentUser = requireCurrentUser();
        Booking booking = findBookingById(bookingId);

        if (!booking.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Ban khong co quyen xac nhan booking nay.");
        }
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Chi co the xac nhan da thue sau khi landlord da duyet lich hen.");
        }

        booking.setStatus(BookingStatus.RENTED);
        Booking saved = bookingRepository.save(booking);

        Post post = postRepository.findById(booking.getPostId()).orElse(null);
        String postTitle = post != null ? post.getTitle() : "phong tro";
        notificationService.sendNotification(
                booking.getLandlordId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.BOOKING_RENTED,
                "Nguoi thue da xac nhan thue tro",
                currentUser.getUsername() + " da xac nhan da thue \"" + postTitle + "\".",
                bookingId
        );

        return bookingMapper.toResponse(saved);
    }

    // ===================== LANDLORD =====================

    @Override
    public List<BookingResponse> getBookingsForLandlord() {
        User currentUser = securityService.getCurrentUser();
        return bookingMapper.toResponseList(
                bookingRepository.findAllByLandlordIdOrderByCreatedAtDesc(currentUser.getId())
        );
    }

    @Override
    public BookingResponse approveBooking(String bookingId) {
        User currentUser = securityService.getCurrentUser();
        Booking booking = findBookingById(bookingId);

        if (!booking.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền duyệt lịch hẹn này.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt lịch hẹn đang ở trạng thái chờ xác nhận.");
        }

        booking.setStatus(BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);

        // Gửi thông báo cho Tenant
        Post post = postRepository.findById(booking.getPostId()).orElse(null);
        String postTitle = post != null ? post.getTitle() : "phòng trọ";
        notificationService.sendNotification(
                booking.getUserId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.BOOKING_APPROVED,
                "Lịch hẹn được chấp nhận! ✅",
                "Chủ trọ đã chấp nhận lịch hẹn xem \"" + postTitle + "\" của bạn.",
                bookingId
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse rejectBooking(String bookingId) {
        User currentUser = securityService.getCurrentUser();
        Booking booking = findBookingById(bookingId);

        if (!booking.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền từ chối lịch hẹn này.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối lịch hẹn đang ở trạng thái chờ xác nhận.");
        }

        booking.setStatus(BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        // Gửi thông báo cho Tenant
        Post post = postRepository.findById(booking.getPostId()).orElse(null);
        String postTitle = post != null ? post.getTitle() : "phòng trọ";
        notificationService.sendNotification(
                booking.getUserId(),
                currentUser.getId(),
                currentUser.getUsername(),
                NotificationType.BOOKING_REJECTED,
                "Lịch hẹn bị từ chối ❌",
                "Rất tiếc, chủ trọ không thể nhận lịch hẹn xem \"" + postTitle + "\" của bạn.",
                bookingId
        );

        return bookingMapper.toResponse(saved);
    }

    @Override
    public List<BookingResponse> getBookings() {
        List<Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        return bookingMapper.toResponseList(bookings);
    }

    // ===================== CHUNG =====================

    @Override
    public BookingResponse getBookingById(String bookingId) {
        User currentUser = securityService.getCurrentUser();
        Booking booking = findBookingById(bookingId);

        if (!booking.getUserId().equals(currentUser.getId())
                && !booking.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xem lịch hẹn này.");
        }

        return bookingMapper.toResponse(booking);
    }

    // ===================== PRIVATE =====================

    private User requireCurrentUser() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Ban can dang nhap de su dung chuc nang nay.");
        }
        return currentUser;
    }

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + bookingId));
    }
}
