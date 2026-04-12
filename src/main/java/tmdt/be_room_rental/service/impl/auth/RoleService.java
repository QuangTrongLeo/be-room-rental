package tmdt.be_room_rental.service.impl.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.res.auth.RoleResponse;
import tmdt.be_room_rental.mapper.auth.RoleMapper;
import tmdt.be_room_rental.service.interfaces.auth.IRoleService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RoleMapper roleMapper;

    @Override
    public List<RoleResponse> getRoles() {
        return roleMapper.toResponseList();
    }
}
