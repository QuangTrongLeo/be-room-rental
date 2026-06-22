package tmdt.be_room_rental.controller.report;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.report.ReportRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.report.ReportResponse;
import tmdt.be_room_rental.service.interfaces.report.IReportService;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<ReportResponse> createReport(@RequestBody @Valid ReportRequest request) {
        return ApiResponse.<ReportResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Gửi báo cáo thành công.")
                .data(reportService.createReport(request))
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'LANDLORD')")
    public ApiResponse<List<ReportResponse>> getMyReports() {
        return ApiResponse.<List<ReportResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy lịch sử báo cáo thành công.")
                .data(reportService.getMyReports())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReportResponse>> getAllReports() {
        return ApiResponse.<List<ReportResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách báo cáo thành công.")
                .data(reportService.getAllReports())
                .build();
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> resolveReport(@PathVariable String id) {
        return ApiResponse.<ReportResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Đã đánh dấu báo cáo là đã xử lý.")
                .data(reportService.resolveReport(id))
                .build();
    }
}
