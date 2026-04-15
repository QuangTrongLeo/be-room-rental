package tmdt.be_room_rental.repository.post;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Amenity;

import java.util.List;

@Repository
public interface AmenityRepository extends MongoRepository<Amenity, String> {
    boolean existsByName(String name);
    List<Amenity> findAllByOrderByCreatedAtDesc();
}
