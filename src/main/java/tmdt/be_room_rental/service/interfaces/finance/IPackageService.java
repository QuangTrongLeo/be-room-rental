package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.req.finance.PackageRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.PackageResponse;

import java.util.List;

public interface IPackageService {
    List<EnumResponse> getPackageTypes();
    List<EnumResponse> getPackageTiers();

    PackageResponse createPackage(PackageRequest request);
    PackageResponse updatePackage(String id, PackageRequest request);
    PackageResponse getPackageById(String id);
    List<PackageResponse> getPackages();
    void deletePackage(String id);
}
