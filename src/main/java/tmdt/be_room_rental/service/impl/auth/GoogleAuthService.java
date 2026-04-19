package tmdt.be_room_rental.service.impl.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.auth.GoogleLoginRequest;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.enums.type.ProviderType;
import tmdt.be_room_rental.mapper.auth.TokenMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenMapper tokenMapper;

    @Value("${app.google.client-id}")
    private String googleClientId;

    /**
     * Xử lý đăng nhập bằng Google:
     * 1. Verify ID Token với Google
     * 2. Lấy thông tin user từ token
     * 3. Tìm hoặc tạo user trong DB
     * 4. Tạo JWT tokens
     */
    public TokenResponse loginWithGoogle(GoogleLoginRequest request) {
        // 1. Verify Google ID Token
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

        // 2. Lấy thông tin user từ Google payload
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 3. Tìm hoặc tạo user
        User user = findOrCreateGoogleUser(email, name, picture);

        // 4. Tạo JWT tokens
        String accessToken = jwtService.generateToken(user, "ACCESS");
        String refreshToken = jwtService.generateToken(user, "REFRESH");

        return tokenMapper.toResponse(accessToken, refreshToken);
    }

    /**
     * Verify Google ID Token bằng Google API Client
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new RuntimeException("Google ID Token không hợp lệ");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Kiểm tra email đã verified bởi Google
            if (!payload.getEmailVerified()) {
                throw new RuntimeException("Email Google chưa được xác thực");
            }

            return payload;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google Token: " + e.getMessage());
        }
    }

    /**
     * Tìm user theo email.
     * - Nếu đã có user (GOOGLE provider) → trả về user
     * - Nếu đã có user (EMAIL provider) → liên kết với Google (cập nhật provider, avatar)
     * - Nếu chưa có → tạo user mới
     */
    private User findOrCreateGoogleUser(String email, String name, String picture) {
        // Tìm user Google trước
        Optional<User> googleUser = userRepository.findByEmailAndProvider(email, ProviderType.GOOGLE);
        if (googleUser.isPresent()) {
            return googleUser.get();
        }

        // Tìm user email thường (liên kết tài khoản)
        Optional<User> emailUser = userRepository.findByEmail(email);
        if (emailUser.isPresent()) {
            User existing = emailUser.get();
            existing.setProvider(ProviderType.GOOGLE);
            if (existing.getAvatar() == null || existing.getAvatar().isBlank()) {
                existing.setAvatar(picture != null ? picture : "");
            }
            existing.setVerified(true);
            return userRepository.save(existing);
        }

        // Tạo user mới
        User newUser = User.builder()
                .username(name != null ? name : email.split("@")[0])
                .email(email)
                .password(null) // Google user không cần password
                .phone(null)
                .avatar(picture != null ? picture : "")
                .role(RoleEnum.USER)
                .isVerified(true) // Google đã xác thực email
                .isActive(true)
                .provider(ProviderType.GOOGLE)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }
}
