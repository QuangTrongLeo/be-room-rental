package tmdt.be_room_rental.service.impl.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.report.ReportRequest;
import tmdt.be_room_rental.dto.res.report.ReportResponse;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.Report;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.BookingStatus;
import tmdt.be_room_rental.enums.status.ReportStatus;
import tmdt.be_room_rental.enums.type.ReportType;
import tmdt.be_room_rental.mapper.report.ReportMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.BookingRepository;
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
    private final BookingRepository bookingRepository;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse createReport(ReportRequest request) {
        User reporter = requireCurrentUser();
        ReportType reportType;
        User target = null;
        Post targetPost = null;

        if (reporter.getRole() == RoleEnum.USER) {
            targetPost = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Phong bi bao cao khong ton tai"));
            target = userRepository.findById(targetPost.getLandlordId())
                    .orElseThrow(() -> new RuntimeException("Chu tro cua phong khong ton tai"));
            if (target.getRole() != RoleEnum.LANDLORD) {
                throw new RuntimeException("Bai dang khong thuoc tai khoan LANDLORD");
            }
            if (!bookingRepository.existsByUserIdAndPostIdAndStatus(
                    reporter.getId(), targetPost.getId(), BookingStatus.RENTED)) {
                throw new RuntimeException("Ban chi co the bao cao phong sau khi da xac nhan thue tro.");
            }
            reportType = ReportType.ROOM;
        } else if (reporter.getRole() == RoleEnum.LANDLORD) {
            target = userRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Nguoi thue bi bao cao khong ton tai"));
            if (target.getRole() != RoleEnum.USER) {
                throw new RuntimeException("LANDLORD chi co the bao cao USER");
            }
            if (!bookingRepository.existsByLandlordIdAndUserIdAndStatus(
                    reporter.getId(), target.getId(), BookingStatus.RENTED)) {
                throw new RuntimeException("Chi co the bao cao USER da xac nhan thue tro voi ban.");
            }
            reportType = ReportType.USER;
        } else {
            throw new RuntimeException("Vai tro hien tai khong the gui bao cao");
        }

        if (reportRepository.existsByUserIdAndTargetIdAndStatus(
                reporter.getId(), request.getTargetId(), ReportStatus.PENDING)) {
            throw new RuntimeException("Ban da co mot bao cao dang cho xu ly cho doi tuong nay");
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
                .orElseThrow(() -> new RuntimeException("Bao cao khong ton tai"));
        report.setStatus(ReportStatus.RESOLVED);
        return toResponse(reportRepository.save(report));
    }

    private User requireCurrentUser() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Ban can dang nhap de su dung tinh nang bao cao");
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
