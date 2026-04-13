package tmdt.be_room_rental.service.interfaces.auth;

import tmdt.be_room_rental.dto.req.auth.ProfileRequest;
import tmdt.be_room_rental.dto.res.auth.UserResponse;
import tmdt.be_room_rental.entity.User;

import java.util.List;

public interface IUserService {
    UserResponse getMyProfile();
    UserResponse getUserById(String id);
    UserResponse updateMyProfile(ProfileRequest request);
    List<UserResponse> getAllUsers();
}
