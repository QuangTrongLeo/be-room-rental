package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.req.auth.*;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;

public interface IAuthService {
    void register(RegisterRequest request);
    void verifyOtp(VerifyOtpRequest request);
    void forgetPassword(ForgetPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    TokenResponse login(LoginRequest request);
}
