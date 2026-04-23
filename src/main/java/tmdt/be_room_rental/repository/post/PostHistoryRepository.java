package tmdt.be_room_rental.repository.post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.PostHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostHistoryRepository extends MongoRepository<PostHistory, String> {
    Optional<PostHistory> findByUserIdAndPostId(String userId, String postId);
    List<PostHistory> findAllByUserIdOrderByViewedAtAsc(String userId);
    List<PostHistory> findAllByUserIdOrderByViewedAtDesc(String userId);
}
