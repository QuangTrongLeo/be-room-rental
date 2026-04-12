package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.req.auth.LoginRequest;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.dto.req.auth.VerifyOtpRequest;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;

public interface IAuthService {
    void register(RegisterRequest request);
    void verifyOtp(VerifyOtpRequest request);
    TokenResponse login(LoginRequest request);
}
