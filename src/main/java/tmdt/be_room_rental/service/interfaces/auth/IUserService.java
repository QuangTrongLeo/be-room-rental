package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;

import java.util.List;

public interface IUserService {
    UserResponse getMyProfile();
    UserResponse getUserById(String id);
    List<UserResponse> getAllUsers();
}
