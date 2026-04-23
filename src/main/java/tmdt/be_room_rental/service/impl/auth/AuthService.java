package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.req.auth.*;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.mapper.auth.TokenMapper;
import tmdt.be_room_rental.repository.auth.UserRepository;
import tmdt.be_room_rental.service.interfaces.auth.IAuthService;
import tmdt.be_room_rental.service.interfaces.auth.IOtpService;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final IOtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenMapper tokenMapper;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        // 2. Tạo User mới (Chưa verified)
        User user = userService.createUser(request);

        // 3. Tạo và gửi OTP
        otpService.createAndSendOtp(user.getEmail());
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request);
    }

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }
        otpService.createAndSendOtp(request.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        VerifyOtpRequest verifyRequest = new VerifyOtpRequest();
        verifyRequest.setEmail(request.getEmail());
        verifyRequest.setOtp(request.getOtp());

        otpService.verifyOtp(verifyRequest);

        User user = userService.getUserByEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }
        if (!user.isVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực OTP");
        }
        String accessToken = jwtService.generateToken(user, "ACCESS");
        String refreshToken = jwtService.generateToken(user, "REFRESH");
        return tokenMapper.toResponse(accessToken, refreshToken);
    }
}