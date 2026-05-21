package tmdt.be_room_rental.controller.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.finance.FinanceResponse;
import tmdt.be_room_rental.service.interfaces.finance.IFinanceService;

import java.util.List;

@RestController
@RequestMapping("/finances")
@RequiredArgsConstructor
public class FinanceController {

    private final IFinanceService financeService;

    @GetMapping("/statistics/month")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FinanceResponse>> getFinanceStatsByMonth() {
        return ApiResponse.<List<FinanceResponse>>builder()
                .code(200)
                .message("Lấy thống kê tài chính theo tháng thành công.")
                .data(financeService.getFinanceStatsByMonth())
                .build();
    }

    @GetMapping("/statistics/quarter")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FinanceResponse>> getFinanceStatsByQuarter() {
        return ApiResponse.<List<FinanceResponse>>builder()
                .code(200)
                .message("Lấy thống kê tài chính theo quý thành công.")
                .data(financeService.getFinanceStatsByQuarter())
                .build();
    }

    @GetMapping("/statistics/year")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FinanceResponse>> getFinanceStatsByYear() {
        return ApiResponse.<List<FinanceResponse>>builder()
                .code(200)
                .message("Lấy thống kê tài chính theo năm thành công.")
                .data(financeService.getFinanceStatsByYear())
                .build();
    }
}