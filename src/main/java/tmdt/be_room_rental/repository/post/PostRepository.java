package tmdt.be_room_rental.repository.post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.enums.PostStatus;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByLandlordIdOrderByCreatedAtDesc(String landlordId);
    List<Post> findAllByStatusOrderByCreatedAtDesc(PostStatus status);
    List<Post> findAllByOrderByCreatedAtDesc();
}
