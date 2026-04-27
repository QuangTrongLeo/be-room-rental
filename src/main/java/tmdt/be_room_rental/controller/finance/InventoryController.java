package tmdt.be_room_rental.controller.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.finance.InventoryResponse;
import tmdt.be_room_rental.service.interfaces.finance.IInventoryService;

import java.util.List;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final IInventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<InventoryResponse>> getInventories() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .code(200)
                .message("Lấy danh sách toàn bộ kho thành công.")
                .data(inventoryService.getInventories())
                .build();
    }

    @GetMapping("/my-inventories")
    @PreAuthorize("hasRole('LANDLORD')")
    public ApiResponse<List<InventoryResponse>> getMyInventories() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .code(200)
                .message("Lấy thông tin kho cá nhân thành công.")
                .data(inventoryService.getMyInventories())
                .build();
    }
}