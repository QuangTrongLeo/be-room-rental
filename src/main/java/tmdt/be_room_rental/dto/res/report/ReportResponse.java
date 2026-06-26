package tmdt.be_room_rental.dto.res.report;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.ReportStatus;
import tmdt.be_room_rental.enums.type.ReportType;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private String id;
    private String reporterId;
    private String reporterUsername;
    private RoleEnum reporterRole;  
    private String targetId;
    private String targetTitle;
    private String targetUsername;
    private RoleEnum targetRole;
    private ReportType type;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
