package tmdt.be_room_rental.mapper.finance;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.finance.InventoryResponse;
import tmdt.be_room_rental.entity.Inventory;

import java.util.List;

@Component
public class InventoryMapper {
    public InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) return null;
        InventoryResponse res = new InventoryResponse();
        res.setId(inventory.getId());
        res.setUserId(inventory.getUserId());
        res.setType(inventory.getType().name());
        res.setTier(inventory.getTier().name());
        res.setBalance(inventory.getBalance());
        return res;
    }

    public List<InventoryResponse> toResponseList(List<Inventory> inventories) {
        return inventories.stream().map(this::toResponse).toList();
    }
}
