package tmdt.be_room_rental.mapper.report;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.report.ReportResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.Report;
import tmdt.be_room_rental.entity.User;

@Component
public class ReportMapper {

    public ReportResponse toResponse(Report report, User reporter, User target, Post targetPost) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getUserId())
                .reporterUsername(reporter != null ? reporter.getUsername() : null)
                .reporterRole(reporter != null ? reporter.getRole() : null)
                .targetId(report.getTargetId())
                .targetTitle(targetPost != null ? targetPost.getTitle() : null)
                .targetUsername(target != null ? target.getUsername() : null)
                .targetRole(target != null ? target.getRole() : null)
                .type(report.getType())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
