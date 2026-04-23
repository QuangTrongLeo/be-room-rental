package tmdt.be_room_rental.repository.auth;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.type.ProviderType;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndProvider(String email, ProviderType provider);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
