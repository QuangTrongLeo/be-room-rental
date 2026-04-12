package tmdt.be_room_rental.repository.room;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Amenity;

@Repository
public interface AmenityRepository extends MongoRepository<Amenity, String> {
    boolean existsByName(String name);
}
