package tmdt.be_room_rental.controller.room;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.room.RoomRequest;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.room.RoomResponse;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.service.interfaces.room.IRoomService;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final IRoomService roomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<RoomResponse> createRoom(@ModelAttribute @Valid RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ApiResponse.<RoomResponse>builder()
                .code(200)
                .message("Tạo phòng trọ thành công.")
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<RoomResponse> updateRoom(@PathVariable String id, @ModelAttribute @Valid RoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ApiResponse.<RoomResponse>builder()
                .code(200)
                .message("Cập nhật phòng trọ thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD')")
    public ApiResponse<Void> deleteRoom(@PathVariable String id) {
        roomService.deleteRoom(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa phòng trọ thành công.")
                .data(null)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable String id) {
        RoomResponse response = roomService.getRoomById(id);
        return ApiResponse.<RoomResponse>builder()
                .code(200)
                .message("Lấy thông tin phòng trọ thành công.")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoomResponse>> getRooms() {
        List<RoomResponse> response = roomService.getRooms();
        return ApiResponse.<List<RoomResponse>>builder()
                .code(200)
                .message("Lấy danh sách phòng trọ thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/types")
    public ApiResponse<List<RoomTypeResponse>> getRoomTypes() {
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .code(200)
                .message("Lấy danh sách loại phòng thành công.")
                .data(roomService.getRoomTypes())
                .build();
    }
}