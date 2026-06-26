package tmdt.be_room_rental.repository.post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Booking;
import tmdt.be_room_rental.enums.status.BookingStatus;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    // Tenant xem lịch đặt của mình
    List<Booking> findAllByUserIdOrderByCreatedAtDesc(String userId);
    // Landlord xem yêu cầu đặt phòng gửi đến
    List<Booking> findAllByLandlordIdOrderByCreatedAtDesc(String landlordId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    // Lọc theo trạng thái
    List<Booking> findAllByUserIdAndStatusOrderByCreatedAtDesc(String userId, BookingStatus status);
    List<Booking> findAllByLandlordIdAndStatusOrderByCreatedAtDesc(String landlordId, BookingStatus status);
    // Kiểm tra trùng booking
    boolean existsByUserIdAndPostIdAndStatus(String userId, String postId, BookingStatus status);
}
