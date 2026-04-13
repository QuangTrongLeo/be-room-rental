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
        // 1. Kiểm tra số lượng ảnh từ request (List<MultipartFile>)
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new RuntimeException("Chỉ được upload tối đa 8 ảnh");
        }

        // 2. Lấy thông tin chủ trọ
        User currentUser = securityService.getCurrentUser();

        // 3. Khởi tạo Entity
        Room room = buildRoom(request, currentUser.getId());

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse updateRoom(String id, RoomRequest request) {
        // 1. Tìm phòng cũ trong DB
        Room room = findRoomById(id);
        User currentUser = securityService.getCurrentUser();

        // 2. Kiểm tra quyền sở hữu
        if (!room.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa phòng này");
        }

        // 3. Cập nhật các trường thông tin cơ bản (Nếu có giá trị mới gửi lên)
        if (request.getName() != null) room.setName(request.getName());
        if (request.getAddress() != null) room.setAddress(request.getAddress());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getArea() > 0) room.setArea(request.getArea()); // Với primitive double, check > 0
        if (request.getPrice() > 0) room.setPrice(request.getPrice());
        if (request.getAmenities() != null) room.setAmenities(request.getAmenities());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());

        // 4. Cập nhật tọa độ
        if (request.getLongitude() != null && request.getLatitude() != null) {
            room.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        }

        // 5. Xử lý logic 8 ảnh
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Kiểm tra số lượng ảnh mới gửi lên
            if (request.getImages().size() > MAX_IMAGES) {
                throw new RuntimeException("Chỉ được upload tối đa " + MAX_IMAGES + " ảnh");
            }

            // Xóa dàn ảnh cũ trên Cloudinary để thay thế bằng dàn ảnh mới hoàn toàn
            if (room.getImages() != null) {
                room.getImages().forEach(cloudinaryService::deleteByUrl);
            }

            // Upload dàn ảnh mới
            List<String> newUrls = request.getImages().stream()
                    .map(file -> cloudinaryService.upload(file, "rooms"))
                    .collect(Collectors.toList());
            room.setImages(newUrls);
        }

        // 6. Lưu lại vào DB
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
    public void deleteRoom(String id) {
        Room room = findRoomById(id);
        User currentUser = securityService.getCurrentUser();

        if (!room.getLandlordId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền xóa");
        }

        // Xóa ảnh trên Cloudinary
        if (room.getImages() != null) {
            room.getImages().forEach(cloudinaryService::deleteByUrl);
        }
        roomRepository.delete(room);
    }

    @Override
    public List<RoomTypeResponse> getRoomTypes() {
        return roomMapper.toTypeResponseList();
    }

    public Room buildRoom(RoomRequest request, String landlordId) {
        Room room = Room.builder()
                .landlordId(landlordId)
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .area(request.getArea())
                .price(request.getPrice())
                .amenities(request.getAmenities())
                .roomType(request.getRoomType())
                .location(new GeoJsonPoint(request.getLongitude(), request.getLatitude()))
                .createdAt(LocalDateTime.now())
                .build();

        // 4. Upload ảnh lên Cloudinary và gán URL vào room
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
