package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.auth.LoginRequest;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.dto.req.auth.VerifyOtpRequest;
import tmdt.be_room_rental.dto.res.TokenResponse;
import tmdt.be_room_rental.service.interfaces.IAuthService;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {

    }

    @Override
    public TokenResponse login(LoginRequest request) {
        return null;
    }
}
