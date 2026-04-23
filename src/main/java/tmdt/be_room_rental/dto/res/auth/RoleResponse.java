package tmdt.be_room_rental.dto.res.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private String role;
}
