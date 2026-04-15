package tmdt.be_room_rental.controller.post;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.post.AmenityRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.post.AmenityResponse;
import tmdt.be_room_rental.service.interfaces.post.IAmenityService;

import java.util.List;

@RestController
@RequestMapping("/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final IAmenityService amenityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AmenityResponse> createAmenity(@RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.createAmenity(request);
        return ApiResponse.<AmenityResponse>builder()
                .code(200)
                .message("Tạo tiện nghi thành công.")
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AmenityResponse> updateAmenity(@PathVariable String id, @RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.updateAmenity(id, request);
        return ApiResponse.<AmenityResponse>builder()
                .code(200)
                .message("Cập nhật tiện nghi thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAmenity(@PathVariable String id) {
        amenityService.deleteAmenity(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa tiện nghi thành công.")
                .data(null)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AmenityResponse> getAmenity(@PathVariable String id) {
        AmenityResponse response = amenityService.getAmenityById(id);
        return ApiResponse.<AmenityResponse>builder()
                .code(200)
                .message("Lấy tiện nghi thành công.")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<AmenityResponse>> getAmenities() {
        List<AmenityResponse> response = amenityService.getAmenities();
        return ApiResponse.<List<AmenityResponse>>builder()
                .code(200)
                .message("Lấy danh sách tiện nghi thành công.")
                .data(response)
                .build();
    }
}