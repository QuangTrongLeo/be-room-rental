package tmdt.be_room_rental.mapper.enums;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.enums.status.OrderStatus;
import tmdt.be_room_rental.enums.type.PackageType;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderEnumMapper {
    public EnumResponse toStatusResponse(OrderStatus status) {
        if (status == null) return null;
        return EnumResponse.builder()
                .value(status.name())
                .build();
    }

    public List<EnumResponse> toStatusResponseList() {
        return Arrays.stream(OrderStatus.values())
                .map(this::toStatusResponse)
                .toList();
    }
}
