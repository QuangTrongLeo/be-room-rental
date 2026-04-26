package tmdt.be_room_rental.repository.finance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends MongoRepository<Packages, String> {
    List<Packages> findAllByOrderByCreatedAtAsc();
    Optional<Packages> findByTypeAndTier(PackageType type, PackageTier tier);
}
