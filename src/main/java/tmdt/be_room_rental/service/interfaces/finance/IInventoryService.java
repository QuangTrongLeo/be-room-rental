package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.res.finance.InventoryResponse;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;

import java.util.List;

public interface IInventoryService {
    List<InventoryResponse> getInventories();
    List<InventoryResponse> getMyInventories();
    void addPackageToInventory(String userId, Packages pkg);
    void checkInventoryAvailability(String userId, PackageType type, PackageTier tier);
}
