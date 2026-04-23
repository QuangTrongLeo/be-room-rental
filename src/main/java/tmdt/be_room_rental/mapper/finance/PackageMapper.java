package tmdt.be_room_rental.mapper.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.finance.PackageResponse;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.mapper.enums.PackageEnumMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PackageMapper {
    private final PackageEnumMapper enumMapper;

    public PackageResponse toResponse(Packages entity) {
        if (entity == null) return null;
        return PackageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(enumMapper.toTypeResponse(entity.getType()))
                .tier(enumMapper.toTierResponse(entity.getTier()))
                .limitQuota(entity.getLimitQuota())
                .activeDays(entity.getActiveDays())
                .price(entity.getPrice())
                .build();
    }

    public List<PackageResponse> toResponseList(List<Packages> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}