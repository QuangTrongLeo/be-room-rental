package tmdt.be_room_rental.service.impl.room;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.room.RoomRequest;
import tmdt.be_room_rental.dto.res.room.RoomResponse;
import tmdt.be_room_rental.dto.res.room.RoomTypeResponse;
import tmdt.be_room_rental.entity.Room;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.mapper.room.RoomMapper;
import tmdt.be_room_rental.repository.room.RoomRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.auth.ICloudinaryService;
import tmdt.be_room_rental.service.interfaces.room.IRoomService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final SecurityService securityService;
    private final ICloudinaryService cloudinaryService;

    private static final int MAX_IMAGES = 8;

    @Override
    public RoomResponse createRoom(RoomRequest request) {
        // 1. Kiểm tra số lượng ảnh
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new RuntimeException("Chỉ được upload tối đa " + MAX_IMAGES + " ảnh");
        }

        // 2. Lấy thông tin chủ trọ
        User currentUser = securityService.getCurrentUser();

        // 3. Khởi tạo Entity thông qua hàm buildRoom
        Room room = buildRoom(request, currentUser.getId());
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse updateRoom(String id, RoomRequest request) {
        // 1. Tìm phòng và kiểm tra quyền
        Room room = findRoomById(id);
        User currentUser = securityService.getCurrentUser();
        if (!room.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa phòng này");
        }

        // 2. Cập nhật các trường kỹ thuật (Chỉ cập nhật nếu request có giá trị)
        if (request.getAddress() != null) room.setAddress(request.getAddress());
        if (request.getArea() > 0) room.setArea(request.getArea());
        if (request.getAmenities() != null) room.setAmenities(request.getAmenities());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());

        // 3. Cập nhật tọa độ
        if (request.getLongitude() != null && request.getLatitude() != null) {
            room.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        // 4. Xử lý logic thay thế toàn bộ ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            if (request.getImages().size() > MAX_IMAGES) {
                throw new RuntimeException("Chỉ được upload tối đa " + MAX_IMAGES + " ảnh");
            }
            // Xóa ảnh cũ trên Cloudinary
            if (room.getImages() != null) {
                room.getImages().forEach(cloudinaryService::deleteByUrl);
            }
            // Upload ảnh mới
            List<String> newUrls = request.getImages().stream()
                    .map(file -> cloudinaryService.upload(file, "rooms"))
                    .collect(Collectors.toList());
            room.setImages(newUrls);
        }

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse getRoomById(String id) {
        return roomMapper.toResponse(findRoomById(id));
    }

    @Override
    public List<RoomResponse> getRooms() {
        return roomMapper.toResponseList(roomRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<RoomResponse> getRoomsByLandLord() {
        User currentUser = securityService.getCurrentUser();
        List<Room> rooms = roomRepository.findAllByLandlordIdOrderByCreatedAtDesc(currentUser.getId());
        return roomMapper.toResponseList(rooms);
    }

    @Override
    public void deleteRoom(String id) {
        Room room = findRoomById(id);
        User currentUser = securityService.getCurrentUser();
        if (!room.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền xóa phòng này");
        }
        if (room.getImages() != null) {
            room.getImages().forEach(cloudinaryService::deleteByUrl);
        }
        roomRepository.delete(room);
    }

    @Override
    public List<RoomTypeResponse> getRoomTypes() {
        return roomMapper.toTypeResponseList();
    }

    private Room buildRoom(RoomRequest request, String landlordId) {
        Room room = Room.builder()
                .landlordId(landlordId)
                .address(request.getAddress())
                .area(request.getArea())
                .amenities(request.getAmenities())
                .roomType(request.getRoomType())
                .createdAt(LocalDateTime.now())
                .build();

        // Xử lý tọa độ
        if (request.getLongitude() != null && request.getLatitude() != null) {
            room.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        // Upload ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> urls = request.getImages().stream()
                    .map(file -> cloudinaryService.upload(file, "rooms"))
                    .collect(Collectors.toList());
            room.setImages(urls);
        }

        return room;
    }

    public Room findRoomById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại với ID: " + id));
    }
}
