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
    public ApiResponse<VoucherResponse> create(@RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Tạo voucher thành công.")
                .data(voucherService.createVoucher(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> update(@PathVariable String id, @RequestBody VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Cập nhật thành công.")
                .data(voucherService.updateVoucher(id, request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<VoucherResponse>> getAll() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(200)
                .data(voucherService.getVouchers())
                .build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<VoucherResponse> getByCode(@PathVariable String code) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .data(voucherService.getVoucherByCode(code))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<VoucherResponse> toggleStatus(@PathVariable String id, @RequestParam boolean isActive) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .data(voucherService.toggleVoucherStatus(id, isActive))
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

    @GetMapping("/validate")
    public ApiResponse<Boolean> validate(@RequestParam String code) {
        return ApiResponse.<Boolean>builder()
                .code(200)
                .data(voucherService.isValidVoucher(code))
                .build();
    }

    @GetMapping("/calculate")
    public ApiResponse<Double> calculate(@RequestParam String code, @RequestParam Double totalAmount) {
        return ApiResponse.<Double>builder()
                .code(200)
                .data(voucherService.calculateDiscount(code, totalAmount))
                .build();
    }
}