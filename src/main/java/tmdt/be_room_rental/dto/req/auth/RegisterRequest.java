package tmdt.be_room_rental.dto.req.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
