package tmdt.be_room_rental.service.impl.auth;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tmdt.be_room_rental.service.interfaces.auth.ICloudinaryService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService implements ICloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        try {
            String publicId = folder + "/" + UUID.randomUUID();
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "auto"   // auto detect image / video / mp3
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IllegalStateException("Upload lên Cloudinary thất bại", e);
        }
    }

    @Override
    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            String publicId = extractPublicId(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ignored) {}
    }

    private String extractPublicId(String url) {
        String[] parts = url.split("/");
        String fileWithExt = parts[parts.length - 1];
        return fileWithExt.substring(0, fileWithExt.lastIndexOf("."));
    }
}
