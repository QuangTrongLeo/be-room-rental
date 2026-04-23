package tmdt.be_room_rental.dto.req.finance;

import lombok.Data;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

@Data
public class PackageRequest {
    private String name;
    private PackageType type;
    private PackageTier tier;
    private Integer limitQuota;
    private Integer activeDays;
    private Double price;
}