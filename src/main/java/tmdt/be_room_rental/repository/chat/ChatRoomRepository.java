package tmdt.be_room_rental.repository.chat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tmdt.be_room_rental.entity.ChatRoom;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    // findById(roomId) từ MongoRepository là đủ dùng.
    // roomId là @Id của ChatRoom, format: min(id1,id2)_max(id1,id2)
}
