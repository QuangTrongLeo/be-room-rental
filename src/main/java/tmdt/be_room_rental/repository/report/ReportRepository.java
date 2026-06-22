package tmdt.be_room_rental.repository.report;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.Report;
import tmdt.be_room_rental.enums.status.ReportStatus;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findAllByUserIdOrderByCreatedAtDesc(String userId);

    List<Report> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndTargetIdAndStatus(String userId, String targetId, ReportStatus status);
}
