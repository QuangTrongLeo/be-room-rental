package tmdt.be_room_rental.repository.finance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.enums.status.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByVnpTxnRef(String vnpTxnRef);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findAllByUserIdOrderByCreatedAtDesc(String userId);
}
