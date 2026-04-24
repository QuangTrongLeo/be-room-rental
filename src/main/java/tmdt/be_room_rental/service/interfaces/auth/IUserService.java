package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.req.auth.ProfileRequest;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;

import java.util.List;

public interface IUserService {
    User createUser(RegisterRequest request);
    UserResponse updateMyProfile(ProfileRequest request);
    UserResponse getMyProfile();
    UserResponse getUserById(String id);
    List<UserResponse> getUsers();
    void deleteUser(String id);
}
