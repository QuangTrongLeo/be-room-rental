package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.finance.PackageRequest;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.dto.res.finance.PackageResponse;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.mapper.enums.PackageEnumMapper;
import tmdt.be_room_rental.mapper.finance.PackageMapper;
import tmdt.be_room_rental.repository.finance.PackageRepository;
import tmdt.be_room_rental.service.interfaces.finance.IPackageService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService implements IPackageService {
    private final PackageRepository packageRepository;
    private final PackageEnumMapper packageEnumMapper;
    private final PackageMapper packageMapper;

    @Override
    public List<EnumResponse> getPackageTypes() {
        return packageEnumMapper.toTypeResponseList();
    }

    @Override
    public List<EnumResponse> getPackageTiers() {
        return packageEnumMapper.toPackageTierResponseList();
    }

    public PackageResponse createPackage(PackageRequest request) {
        Packages packages = Packages.builder()
                .name(request.getName())
                .type(request.getType())
                .tier(request.getTier())
                .limitQuota(request.getLimitQuota())
                .activeDays(request.getActiveDays())
                .price(request.getPrice())
                .createdAt(LocalDateTime.now())
                .build();

        return packageMapper.toResponse(packageRepository.save(packages));
    }

    @Override
    public PackageResponse updatePackage(String id, PackageRequest request) {
        Packages packages = findPackageById(id);
        if (request.getName() != null) packages.setName(request.getName());
        if (request.getType() != null) packages.setType(request.getType());
        if (request.getTier() != null) packages.setTier(request.getTier());
        if (request.getLimitQuota() != null) packages.setLimitQuota(request.getLimitQuota());
        if (request.getActiveDays() != null) packages.setActiveDays(request.getActiveDays());
        if (request.getPrice() != null) packages.setPrice(request.getPrice());
        return packageMapper.toResponse(packageRepository.save(packages));
    }

    @Override
    public PackageResponse getPackageById(String id) {
        return packageMapper.toResponse(findPackageById(id));
    }

    @Override
    public List<PackageResponse> getPackages() {
        List<Packages> packages = packageRepository.findAllByOrderByCreatedAtAsc();
        return packageMapper.toResponseList(packages);
    }

    @Override
    public void deletePackage(String id) {
        if (!packageRepository.existsById(id)) {
            throw new RuntimeException("Gói dịch vụ không tồn tại để xóa.");
        }
        packageRepository.deleteById(id);
    }

    public Packages findPackageById(String id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gói dịch vụ không tồn tại với ID: " + id));
    }
}
