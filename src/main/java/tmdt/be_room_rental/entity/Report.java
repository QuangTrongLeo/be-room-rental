package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.status.ReportStatus;
import tmdt.be_room_rental.enums.type.ReportType;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String userId;
    private String targetId; // ID của Room hoặc User bị báo cáo
    private ReportType type;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
