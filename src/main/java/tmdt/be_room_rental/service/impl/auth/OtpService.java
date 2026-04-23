package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.req.auth.VerifyOtpRequest;
import tmdt.be_room_rental.entity.OtpVerification;
import tmdt.be_room_rental.repository.auth.OtpVerificationRepository;
import tmdt.be_room_rental.service.interfaces.auth.IOtpService;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserService userService;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public void createAndSendOtp(String email) {
        // 1. Tạo mã OTP 6 số
        String otp = generateNumericOtp();
        // 2. Xóa các OTP cũ của email này (nếu có) để tránh rác database
        otpRepository.deleteByEmail(email);
        // 3. Lưu OTP mới
        saveOtpEntry(email, otp);
        // 4. Gửi mail
        emailService.sendOtp(email, otp);
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        // 1. Tìm OTP trong DB
        OtpVerification otpVerification = getValidOtp(request.getEmail(), request.getOtp());
        // 2. Kiểm tra hết hạn
        if (otpVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpVerification);
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        // 3. Xác thực User
        userService.enableUser(request.getEmail());
        // 4. Xóa mã OTP sau khi dùng xong thành công
        otpRepository.delete(otpVerification);
    }

    private String generateNumericOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void saveOtpEntry(String email, String otp) {
        otpRepository.deleteByEmail(email);

        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpRepository.save(otpVerification);
    }

    private OtpVerification getValidOtp(String email, String otp) {
        return otpRepository
                .findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new RuntimeException("Mã OTP không chính xác"));
    }
}