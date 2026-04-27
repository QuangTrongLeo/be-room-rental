package tmdt.be_room_rental.mapper.enums;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.enums.type.PackageType;
import tmdt.be_room_rental.enums.type.PackageTier;

import java.util.Arrays;
import java.util.List;

@Component
public class PackageEnumMapper {

    public EnumResponse toTypeResponse(PackageType type) {
        if (type == null) return null;
        return EnumResponse.builder()
                .value(type.name())
                .build();
    }

    public List<EnumResponse> toTypeResponseList() {
        return Arrays.stream(PackageType.values())
                .map(this::toTypeResponse)
                .toList();
    }

    public EnumResponse toTierResponse(PackageTier tier) {
        if (tier == null) return null;
        return EnumResponse.builder()
                .value(tier.name())
                .build();
    }

    public List<EnumResponse> toPackageTierResponseList() {
        return Arrays.stream(PackageTier.values())
                .map(this::toTierResponse)
                .toList();
    }
}