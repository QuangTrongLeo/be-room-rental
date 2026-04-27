package tmdt.be_room_rental.controller.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.finance.PackageRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.PackageResponse;
import tmdt.be_room_rental.service.interfaces.finance.IPackageService;

import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {

    private final IPackageService packageService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PackageResponse> createPackage(@RequestBody PackageRequest request) {
        return ApiResponse.<PackageResponse>builder()
                .code(200)
                .message("Tạo gói dịch vụ thành công.")
                .data(packageService.createPackage(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PackageResponse> updatePackage(
            @PathVariable String id,
            @RequestBody PackageRequest request) {
        return ApiResponse.<PackageResponse>builder()
                .code(200)
                .message("Cập nhật gói dịch vụ thành công.")
                .data(packageService.updatePackage(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deletePackage(@PathVariable String id) {
        packageService.deletePackage(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa gói dịch vụ vĩnh viễn.")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<PackageResponse>> getPackages() {
        return ApiResponse.<List<PackageResponse>>builder()
                .code(200)
                .data(packageService.getPackages())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<PackageResponse> getPackage(@PathVariable String id) {
        return ApiResponse.<PackageResponse>builder()
                .code(200)
                .data(packageService.getPackageById(id))
                .build();
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<EnumResponse>> getPackageTypes() {
        return ApiResponse.<List<EnumResponse>>builder()
                .code(200)
                .data(packageService.getPackageTypes())
                .build();
    }

    @GetMapping("/tiers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<List<EnumResponse>> getPackageTiers() {
        return ApiResponse.<List<EnumResponse>>builder()
                .code(200)
                .data(packageService.getPackageTiers())
                .build();
    }
}