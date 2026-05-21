package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.repository.auth.UserRepository;

@Component
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu chưa xác thực hoặc là user ẩn danh (chưa đăng nhập)
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElse(null); // Trả về null thay vì ném lỗi để logic phía sau tự xử lý
    }
}
