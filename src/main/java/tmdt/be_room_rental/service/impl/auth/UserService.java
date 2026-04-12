package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.ProviderType;
import tmdt.be_room_rental.enums.RoleEnum;
import tmdt.be_room_rental.mapper.auth.UserMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.service.interfaces.auth.IUserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final UserMapper userMapper;

    @Override
    public UserResponse getMyProfile() {
        User user = securityService.getCurrentUser();
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = getById(id);
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    public User createUser(RegisterRequest request) {
        RoleEnum role = determineRole(request.getRole());

        ProviderType provider = determineProvider(request.getProvider());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .isVerified(false)
                .isActive(true)
                .provider(provider)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    public User getById(String id){
        return userRepository.findById(id)
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

    private ProviderType determineProvider(ProviderType requestedProvider) {
        if (requestedProvider == null) {
            return ProviderType.EMAIL;
        }
        return requestedProvider;
    }
}
