package tmdt.be_room_rental.dto.req.auth;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileRequest {
    private String username;
    private String phone;
    private MultipartFile avatar;
}
