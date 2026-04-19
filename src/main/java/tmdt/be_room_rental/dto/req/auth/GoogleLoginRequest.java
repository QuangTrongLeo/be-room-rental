package tmdt.be_room_rental.dto.req.auth;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}
