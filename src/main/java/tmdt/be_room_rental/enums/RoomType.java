package tmdt.be_room_rental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {
    SINGLE_ROOM("Phòng đơn"),    // Phòng cho 1 người, riêng tư
    SHARED_ROOM("Phòng ghép"),    // Phòng ở chung với người khác để chia tiền
    DORMITORY("Ký túc xá"),       // Dạng giường tầng (Dorm)
    STUDIO("Phòng Studio"),       // Phòng tự do, không vách ngăn
    APARTMENT("Căn hộ");          // Căn hộ nguyên căn

    private final String displayName;
}
