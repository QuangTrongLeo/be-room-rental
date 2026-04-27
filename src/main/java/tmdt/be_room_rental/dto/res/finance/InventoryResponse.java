package tmdt.be_room_rental.dto.res.finance;

import lombok.Data;

@Data
public class InventoryResponse {
    private String id;
    private String userId;
    private String type;
    private String tier;
    private Integer balance;
}
