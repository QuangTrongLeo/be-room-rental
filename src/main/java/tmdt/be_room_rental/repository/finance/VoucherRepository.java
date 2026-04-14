package tmdt.be_room_rental.repository.finance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Voucher;

import java.util.Optional;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
}
