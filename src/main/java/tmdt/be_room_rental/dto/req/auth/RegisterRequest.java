package tmdt.be_room_rental.dto.req.auth;

import lombok.Data;
import tmdt.be_room_rental.enums.type.ProviderType;
import tmdt.be_room_rental.enums.RoleEnum;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private RoleEnum role;
    private ProviderType provider;
}
