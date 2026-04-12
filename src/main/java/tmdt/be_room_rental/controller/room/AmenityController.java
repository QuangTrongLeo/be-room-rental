package tmdt.be_room_rental.controller.room;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.room.AmenityRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.room.AmenityResponse;
import tmdt.be_room_rental.service.interfaces.room.IAmenityService;

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
                .code(HttpStatus.CREATED.value())
                .message("Tạo tiện nghi thành công.")
                .data(response)
                .build();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AmenityResponse> updateAmenity(@RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.updateAmenity(request);
        return ApiResponse.<AmenityResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật tiện nghi thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAmenity(@PathVariable String id) {
        amenityService.deleteAmenity(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa tiện nghi thành công.")
                .data(null)
                .build();
    }

    @GetMapping
    public ApiResponse<List<AmenityResponse>> getAmenities() {
        List<AmenityResponse> response = amenityService.getAmenities();
        return ApiResponse.<List<AmenityResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách tiện nghi thành công.")
                .data(response)
                .build();
    }
}