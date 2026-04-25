package tmdt.be_room_rental.mapper.auth;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .postQuota(user.getPostQuota())
                .boostQuota(user.getBoostQuota())
                .rate(user.getRate())
                .role(user.getRole())
                .build();
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) return List.of();
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
