package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.req.auth.VerifyOtpRequest;

public interface IOtpService {
    void createAndSendOtp(String email);
    void verifyOtp(VerifyOtpRequest request);
}
