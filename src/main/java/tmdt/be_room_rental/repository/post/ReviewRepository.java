package tmdt.be_room_rental.repository.post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findAllByLandlordIdOrderByCreatedAtDesc(String landlordId);
    List<Review> findAllByUserIdOrderByCreatedAtDesc(String userId);
}
