package tmdt.be_room_rental.service.interfaces.chat;

import tmdt.be_room_rental.dto.res.chat.ChatRoomResponse;

public interface IChatRoomService {

    /**
     * Lấy phòng chat hiện có hoặc tạo mới giữa user hiện tại (lấy từ token)
     * và targetUserId được truyền vào.
     *
     * @param targetUserId ID của người dùng muốn chat cùng (USER hoặc LANDLORD)
     * @return ChatRoomResponse chứa roomId để FE kết nối Firebase
     */
    ChatRoomResponse getOrCreateRoom(String targetUserId);
}
