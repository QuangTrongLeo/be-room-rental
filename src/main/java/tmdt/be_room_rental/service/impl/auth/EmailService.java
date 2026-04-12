package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác thực tài khoản VieMp3 Music");
        message.setText("Mã OTP của bạn là: " + otp + " (hết hạn sau 10 phút).");
        mailSender.send(message);
    }
}
