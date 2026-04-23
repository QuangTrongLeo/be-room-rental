package tmdt.be_room_rental.service.impl.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.post.PostHistoryResponse;
import tmdt.be_room_rental.entity.PostHistory;
import tmdt.be_room_rental.mapper.post.PostHistoryMapper;
import tmdt.be_room_rental.repository.post.PostHistoryRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.post.IPostHistoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostHistoryService implements IPostHistoryService {
    private final PostHistoryRepository postHistoryRepository;
    private final PostHistoryMapper postHistoryMapper;
    private final SecurityService securityService;

    private static final int MAX_HISTORY = 30;

    @Override
    public void saveHistory(String userId, String postId) {
        // 1. Kiểm tra bài post đã tồn tại trong lịch sử user chưa
        Optional<PostHistory> existing = postHistoryRepository.findByUserIdAndPostId(userId, postId);

        if (existing.isPresent()) {
            // Nếu rồi: Cập nhật thời gian xem mới nhất (đẩy lên đầu queue)
            PostHistory history = existing.get();
            history.setViewedAt(LocalDateTime.now());
            postHistoryRepository.save(history);
        } else {
            // Nếu chưa: Kiểm tra giới hạn 30 bài
            List<PostHistory> historyList = postHistoryRepository.findAllByUserIdOrderByViewedAtAsc(userId);

            if (historyList.size() >= MAX_HISTORY) {
                // Xóa phần tử cũ nhất
                postHistoryRepository.delete(historyList.get(0));
            }

            // Lưu bài mới
            PostHistory newHistory = PostHistory.builder()
                    .userId(userId)
                    .postId(postId)
                    .viewedAt(LocalDateTime.now())
                    .build();
            postHistoryRepository.save(newHistory);
        }
    }

    @Override
    public List<PostHistoryResponse> getMyPostHistory() {
        String userId = securityService.getCurrentUser().getId();
        List<PostHistory> histories = postHistoryRepository.findAllByUserIdOrderByViewedAtDesc(userId);
        return postHistoryMapper.toResponseList(histories);
    }
}