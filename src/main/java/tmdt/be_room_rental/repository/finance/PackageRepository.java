package tmdt.be_room_rental.repository.finance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Packages;

import java.util.List;

@Repository
public interface PackageRepository extends MongoRepository<Packages, String> {
    List<Packages> findAllByOrderByCreatedAtAsc();
}
