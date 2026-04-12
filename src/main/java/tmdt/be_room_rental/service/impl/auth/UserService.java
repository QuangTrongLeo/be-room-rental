package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.repository.auth.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public User createUser(RegisterRequest request) {
        RoleEnum role = determineRole(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isVerified(false)
                .isActive(true)
                .provider("LOCAL")
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    public void enableUser(String email) {
        User user = getUserByEmail(email);
        user.setVerified(true);
        userRepository.save(user);
    }

    private RoleEnum determineRole(RoleEnum requestedRole) {
        if (requestedRole == null) {
            return RoleEnum.USER;
        }
        return requestedRole;
    }
}
