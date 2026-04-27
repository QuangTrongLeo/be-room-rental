package tmdt.be_room_rental.dto.res.finance;

import lombok.Builder;
import lombok.Data;
import tmdt.be_room_rental.dto.res.enums.EnumResponse;

@Data
@Builder
public class PackageResponse {
    private String id;
    private String name;
    private EnumResponse type;
    private EnumResponse tier;
    private Integer limitQuota;
    private Integer activeDays;
    private Double price;
}