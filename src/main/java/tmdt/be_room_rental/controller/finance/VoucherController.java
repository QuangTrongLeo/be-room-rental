package tmdt.be_room_rental.controller.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.finance.VoucherRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.finance.VoucherResponse;
import tmdt.be_room_rental.service.interfaces.finance.IVoucherService;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> createVoucher(@RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Tạo voucher thành công.")
                .data(voucherService.createVoucher(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> updateVoucher(@PathVariable String id, @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Cập nhật thành công.")
                .data(voucherService.updateVoucher(id, request))
                .build();
    }

    @PutMapping("/active/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> updateVoucherActive(@PathVariable String id, @RequestBody boolean isActive) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .data(voucherService.updateActiveVoucher(id, isActive))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<VoucherResponse>> getVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(200)
                .data(voucherService.getVouchers())
                .build();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<VoucherResponse>> getActiveVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(200)
                .data(voucherService.getActiveVouchers())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<VoucherResponse> getVoucher(@PathVariable String id) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .data(voucherService.getVoucherById(id))
                .build();
    }

    @DeleteMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> softDelete(@PathVariable String id) {
        voucherService.hiddenVoucher(id);
        return ApiResponse.<Void>builder().code(200).message("Đã ẩn voucher.").build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> hardDelete(@PathVariable String id) {
        voucherService.deleteVoucher(id);
        return ApiResponse.<Void>builder().code(200).message("Đã xóa vĩnh viễn.").build();
    }
}