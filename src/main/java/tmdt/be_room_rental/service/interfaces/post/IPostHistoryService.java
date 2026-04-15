package tmdt.be_room_rental.service.interfaces.post;

import tmdt.be_room_rental.dto.res.post.PostHistoryResponse;

import java.util.List;

public interface IPostHistoryService {
    void saveHistory(String userId, String postId);
    List<PostHistoryResponse> getMyPostHistory();
}
