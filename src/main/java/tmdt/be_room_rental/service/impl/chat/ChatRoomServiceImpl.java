package tmdt.be_room_rental.service.impl.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.dto.res.chat.ChatRoomResponse;
import tmdt.be_room_rental.entity.ChatRoom;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.mapper.auth.UserMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.repository.chat.ChatRoomRepository;
import tmdt.be_room_rental.repository.post.BookingRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.chat.IChatRoomService;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements IChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityService securityService;

    @Override
    public ChatRoomResponse getOrCreateRoom(String targetUserId) {
        User currentUser = requireCurrentUser();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng cần nhắn tin."));

        String roomId = buildRoomId(currentUser.getId(), targetUserId);
        validateRoomAccess(currentUser, targetUser, roomId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseGet(() -> createAndSaveRoom(roomId, currentUser.getId(), targetUserId));

        return mapToResponse(chatRoom);
    }

    @Override
    public List<UserResponse> getContacts() {
        User currentUser = requireCurrentUser();
        Set<String> contactIds = new LinkedHashSet<>();

        chatRoomRepository.findAllByParticipantIdsContaining(currentUser.getId()).stream()
                .map(room -> findOtherParticipantId(room, currentUser.getId()))
                .filter(Objects::nonNull)
                .forEach(contactIds::add);

        if (currentUser.getRole() == RoleEnum.LANDLORD) {
            bookingRepository.findAllByLandlordIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                    .map(booking -> booking.getUserId())
                    .filter(Objects::nonNull)
                    .forEach(contactIds::add);
        } else if (currentUser.getRole() == RoleEnum.USER) {
            userRepository.findAll().stream()
                    .filter(user -> user.getRole() == RoleEnum.LANDLORD)
                    .filter(User::isActive)
                    .map(User::getId)
                    .forEach(contactIds::add);
        }

        RoleEnum expectedRole = currentUser.getRole() == RoleEnum.LANDLORD ? RoleEnum.USER : RoleEnum.LANDLORD;
        return contactIds.stream()
                .map(userRepository::findById)
                .flatMap(Optional::stream)
                .filter(user -> user.getRole() == expectedRole)
                .filter(User::isActive)
                .map(userMapper::toResponse)
                .toList();
    }

    private void validateRoomAccess(User currentUser, User targetUser, String roomId) {
        if (currentUser.getRole() == RoleEnum.USER && targetUser.getRole() != RoleEnum.LANDLORD) {
            throw new RuntimeException("Người thuê chỉ có thể nhắn tin với chủ trọ.");
        }

        if (currentUser.getRole() == RoleEnum.LANDLORD && targetUser.getRole() != RoleEnum.USER) {
            throw new RuntimeException("Chủ trọ chỉ có thể nhắn tin với người thuê.");
        }

        if (currentUser.getRole() == RoleEnum.LANDLORD) {
            boolean hasRoom = chatRoomRepository.existsById(roomId);
            boolean hasBooking = bookingRepository.findAllByLandlordIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                    .anyMatch(booking -> targetUser.getId().equals(booking.getUserId()));

            if (!hasRoom && !hasBooking) {
                throw new RuntimeException("Chủ trọ chỉ có thể nhắn tin với người thuê đã có phòng chat hoặc đã đặt lịch.");
            }
        }
    }

    private User requireCurrentUser() {
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chat.");
        }
        return currentUser;
    }

    private String findOtherParticipantId(ChatRoom room, String currentUserId) {
        if (room.getParticipantIds() == null) return null;
        return room.getParticipantIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);
    }

    private String buildRoomId(String id1, String id2) {
        return id1.compareTo(id2) < 0
                ? id1 + "_" + id2
                : id2 + "_" + id1;
    }

    private ChatRoom createAndSaveRoom(String roomId, String currentUserId, String targetUserId) {
        ChatRoom newRoom = ChatRoom.builder()
                .id(roomId)
                .participantIds(List.of(currentUserId, targetUserId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return chatRoomRepository.save(newRoom);
    }

    private ChatRoomResponse mapToResponse(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .participantIds(chatRoom.getParticipantIds())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}
