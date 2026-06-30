package tmdt.be_room_rental.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tmdt.be_room_rental.dto.req.report.ReportRequest;
import tmdt.be_room_rental.dto.res.report.ReportResponse;
import tmdt.be_room_rental.entity.Report;
import tmdt.be_room_rental.entity.Post;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.status.BookingStatus;
import tmdt.be_room_rental.enums.status.ReportStatus;
import tmdt.be_room_rental.mapper.report.ReportMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.post.BookingRepository;
import tmdt.be_room_rental.repository.post.PostRepository;
import tmdt.be_room_rental.repository.report.ReportRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.impl.report.ReportService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private BookingRepository bookingRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                reportRepository,
                userRepository,
                postRepository,
                securityService,
                bookingRepository,
                new ReportMapper()
        );
    }

    @Test
    void userCanReportRoom() {
        User reporter = user("user-1", "Người thuê", RoleEnum.USER);
        User landlord = user("landlord-1", "Chủ trọ", RoleEnum.LANDLORD);
        Post room = Post.builder().id("post-1").landlordId(landlord.getId()).title("Phòng trọ Quận 1").build();
        ReportRequest request = request(room.getId());

        when(securityService.getCurrentUser()).thenReturn(reporter);
        when(postRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(landlord.getId())).thenReturn(Optional.of(landlord));
        when(bookingRepository.existsByUserIdAndPostIdAndStatus(
                reporter.getId(), room.getId(), BookingStatus.RENTED)).thenReturn(true);
        when(reportRepository.existsByUserIdAndTargetIdAndStatus(
                reporter.getId(), room.getId(), ReportStatus.PENDING)).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.createReport(request);

        assertEquals(reporter.getId(), response.getReporterId());
        assertEquals(room.getId(), response.getTargetId());
        assertEquals(room.getTitle(), response.getTargetTitle());
        assertEquals(ReportStatus.PENDING, response.getStatus());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void landlordCannotReportAnotherLandlord() {
        User reporter = user("landlord-1", "Chủ trọ 1", RoleEnum.LANDLORD);
        User target = user("landlord-2", "Chủ trọ 2", RoleEnum.LANDLORD);

        when(securityService.getCurrentUser()).thenReturn(reporter);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThrows(RuntimeException.class, () -> reportService.createReport(request(target.getId())));
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void pendingReportCannotBeDuplicated() {
        User reporter = user("landlord-1", "Chủ trọ", RoleEnum.LANDLORD);
        User target = user("user-1", "Người thuê", RoleEnum.USER);

        when(securityService.getCurrentUser()).thenReturn(reporter);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(bookingRepository.existsByLandlordIdAndUserIdAndStatus(
                reporter.getId(), target.getId(), BookingStatus.RENTED)).thenReturn(true);
        when(reportRepository.existsByUserIdAndTargetIdAndStatus(
                reporter.getId(), target.getId(), ReportStatus.PENDING)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> reportService.createReport(request(target.getId())));
        verify(reportRepository, never()).save(any(Report.class));
    }

    private User user(String id, String username, RoleEnum role) {
        return User.builder().id(id).username(username).role(role).build();
    }

    private ReportRequest request(String targetId) {
        ReportRequest request = new ReportRequest();
        request.setTargetId(targetId);
        request.setReason("Lý do báo cáo hợp lệ");
        return request;
    }
}
