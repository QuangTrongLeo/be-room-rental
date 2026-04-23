package tmdt.be_room_rental.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "packages")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Packages {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name;

    private PackageType type;    // Loại gói: Đăng tin / Đẩy tin
    private PackageTier tier;    // Cấp độ: NORMAL / PRO

    // --- Quyền lợi của gói ---
    private Integer limitQuota;   // Số lượng bài được phép (3 hoặc 5)
    private Integer activeDays;   // Số ngày duy trì cho mỗi bài (15 hoặc 30)

    private Double price;

    private LocalDateTime createdAt;
}
