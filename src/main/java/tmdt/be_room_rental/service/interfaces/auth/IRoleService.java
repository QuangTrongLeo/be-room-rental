package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.res.auth.RoleResponse;

import java.util.List;

public interface IRoleService {
    List<RoleResponse> getRoles();
}
