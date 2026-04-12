package tmdt.be_room_rental.dto.req.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}
