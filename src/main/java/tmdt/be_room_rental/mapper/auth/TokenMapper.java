package tmdt.be_room_rental.mapper.auth;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;

@Component
public class TokenMapper {
    public TokenResponse toResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
