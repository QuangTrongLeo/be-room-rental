package tmdt.be_room_rental.service.impl.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.report.ReportRequest;
import tmdt.be_room_rental.dto.res.report.ReportResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.Report;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.ReportStatus;
import tmdt.be_room_rental.enums.type.ReportType;
import tmdt.be_room_rental.mapper.report.ReportMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.repository.report.ReportRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.report.IReportService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SecurityService securityService;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse createReport(ReportRequest request) {
        User reporter = requireCurrentUser();
        ReportType reportType;
        User target = null;
        Post targetPost = null;

        if (reporter.getRole() == RoleEnum.USER) {
            targetPost = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Phòng bị báo cáo không tồn tại"));
            target = userRepository.findById(targetPost.getLandlordId())
                    .orElseThrow(() -> new RuntimeException("Chủ trọ của phòng không tồn tại"));
            if (target.getRole() != RoleEnum.LANDLORD) {
                throw new RuntimeException("Bài đăng không thuộc tài khoản LANDLORD");
            }
            reportType = ReportType.ROOM;
        } else if (reporter.getRole() == RoleEnum.LANDLORD) {
            target = userRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Người thuê bị báo cáo không tồn tại"));
            if (target.getRole() != RoleEnum.USER) {
                throw new RuntimeException("LANDLORD chỉ có thể báo cáo USER");
            }
            reportType = ReportType.USER;
        } else {
            throw new RuntimeException("Vai trò hiện tại không thể gửi báo cáo");
        }

        if (reportRepository.existsByUserIdAndTargetIdAndStatus(
                reporter.getId(), request.getTargetId(), ReportStatus.PENDING)) {
            throw new RuntimeException("Bạn đã có một báo cáo đang chờ xử lý cho đối tượng này");
        }

        Report report = Report.builder()
                .userId(reporter.getId())
                .targetId(request.getTargetId())
                .type(reportType)
                .reason(request.getReason().trim())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return reportMapper.toResponse(reportRepository.save(report), reporter, target, targetPost);
    }

    @Override
    public List<ReportResponse> getMyReports() {
        User currentUser = requireCurrentUser();
        return toResponseList(reportRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId()));
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return toResponseList(reportRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public ReportResponse resolveReport(String id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Báo cáo không tồn tại"));
        report.setStatus(ReportStatus.RESOLVED);
        return toResponse(reportRepository.save(report));
    }

    private User requireCurrentUser() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng tính năng báo cáo");
        }
        return currentUser;
    }

    private List<ReportResponse> toResponseList(List<Report> reports) {
        return reports.stream().map(this::toResponse).toList();
    }

    private ReportResponse toResponse(Report report) {
        User reporter = userRepository.findById(report.getUserId()).orElse(null);
        Post targetPost = null;
        User target;

        if (report.getType() == ReportType.ROOM) {
            targetPost = postRepository.findById(report.getTargetId()).orElse(null);
            target = targetPost != null
                    ? userRepository.findById(targetPost.getLandlordId()).orElse(null)
                    : null;
        } else {
            target = userRepository.findById(report.getTargetId()).orElse(null);
        }

        return reportMapper.toResponse(report, reporter, target, targetPost);
    }
}
