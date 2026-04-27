package tmdt.be_room_rental.mapper.enums;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;
import tmdt.be_room_rental.enums.status.PostStatus;

import java.util.Arrays;
import java.util.List;

@Component
public class PostEnumMapper {
    public EnumResponse toStatusResponse(PostStatus status) {
        if (status == null) return null;
        return EnumResponse.builder()
                .value(status.name())
                .build();
    }

    public List<EnumResponse> toStatusResponseList() {
        return Arrays.stream(PostStatus.values())
                .map(this::toStatusResponse)
                .toList();
    }
}
