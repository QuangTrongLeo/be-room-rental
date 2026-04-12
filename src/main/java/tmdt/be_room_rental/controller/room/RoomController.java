package tmdt.be_room_rental.controller.room;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.service.interfaces.room.IRoomService;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final IRoomService roomService;

    @GetMapping("/types")
    public ApiResponse<List<RoomTypeResponse>> getRoomTypes() {
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .code(200)
                .message("Lấy danh sách loại phòng thành công.")
                .data(roomService.getRoomTypes())
                .build();
    }
}
