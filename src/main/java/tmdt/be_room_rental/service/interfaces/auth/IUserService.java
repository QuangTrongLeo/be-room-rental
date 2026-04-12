package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;

import java.util.List;

public interface IUserService {
    UserResponse createUser(User user);
    UserResponse updateUser(User user);
    void deleteUser(User user);
    UserResponse getMyProfile(User user);
    UserResponse getUserById(User user);
    List<UserResponse> getAllUsers();
}
