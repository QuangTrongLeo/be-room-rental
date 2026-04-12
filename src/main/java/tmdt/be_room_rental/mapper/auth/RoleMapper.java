package tmdt.be_room_rental.mapper.auth;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.auth.RoleResponse;
import tmdt.be_room_rental.enums.RoleEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper {
    public RoleResponse toResponse(RoleEnum role) {
        if (role == null) return null;

        return RoleResponse.builder()
                .role(role.name())
                .build();
    }

    public List<RoleResponse> toResponseList() {
        return Arrays.stream(RoleEnum.values())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
