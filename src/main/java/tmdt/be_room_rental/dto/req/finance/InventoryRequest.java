package tmdt.be_room_rental.dto.req.finance;

import lombok.Data;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

@Data
public class InventoryRequest {
    private String userId;
    private PackageType type;
    private PackageTier tier;
    private Integer balance;
}
