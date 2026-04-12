package tmdt.be_room_rental.service.interfaces.auth;

import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {
    String upload(MultipartFile file, String folder);
    void deleteByUrl(String fileUrl);
}
