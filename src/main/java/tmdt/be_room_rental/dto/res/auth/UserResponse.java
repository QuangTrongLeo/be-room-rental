package tmdt.be_room_rental.dto.res.auth;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.enums.RoleEnum;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private RoleEnum role;
}
