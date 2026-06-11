package tmdt.be_room_rental.repository.notification;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    // Lấy tất cả thông báo của 1 user, mới nhất trước
    List<Notification> findAllByRecipientIdOrderByCreatedAtDesc(String recipientId);

    // Đếm số thông báo chưa đọc
    long countByRecipientIdAndIsRead(String recipientId, boolean isRead);
}
