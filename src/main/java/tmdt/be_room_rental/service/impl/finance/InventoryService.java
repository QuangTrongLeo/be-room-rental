package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmdt.be_room_rental.dto.res.finance.InventoryResponse;
import tmdt.be_room_rental.entity.Inventory;
import tmdt.be_room_rental.entity.Packages;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.type.PackageTier;
import tmdt.be_room_rental.enums.type.PackageType;
import tmdt.be_room_rental.mapper.finance.InventoryMapper;
import tmdt.be_room_rental.repository.finance.InventoryRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.finance.IInventoryService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService implements IInventoryService {

    private final InventoryRepository inventoryRepository;
    private final SecurityService securityService;
    private final InventoryMapper inventoryMapper;

    @Override
    public List<InventoryResponse> getInventories() {
        return inventoryMapper.toResponseList(inventoryRepository.findAll());
    }

    @Override
    public List<InventoryResponse> getMyInventories() {
        User user = securityService.getCurrentUser();
        return inventoryMapper.toResponseList(inventoryRepository.findAllByUserId(user.getId()));
    }

    @Override
    @Transactional
    public void addPackageToInventory(String userId, Packages pkg) {
        // 1. Lấy hoặc tạo mới inventory dựa trên type và tier của package
        Inventory inventory = findInventory(userId, pkg.getType(), pkg.getTier())
                .orElseGet(() -> buildInventory(userId, pkg));

        // 2. Thực hiện cộng dồn quota an toàn
        accumulateBalance(inventory, pkg.getLimitQuota());

        // 3. Lưu lại vào DB
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkInventoryAvailability(String userId, PackageType type, PackageTier tier) {
        // Tìm inventory, nếu không thấy nghĩa là người dùng chưa bao giờ mua gói này
        Inventory inventory = findInventory(userId, type, tier)
                .orElseThrow(() -> new RuntimeException("Bạn không sở hữu gói " + type + " cấp độ " + tier));

        // Kiểm tra số dư lượt dùng
        if (isBalanceEmpty(inventory)) {
            throw new RuntimeException("Gói " + type + " cấp độ " + tier + " của bạn đã hết lượt sử dụng.");
        }
    }

    @Transactional
    public void consumeInventory(String userId, PackageType type, PackageTier tier) {
        Inventory inventory = findInventory(userId, type, tier)
                .orElseThrow(() -> new RuntimeException("Người dùng không có gói " + type + " cấp độ " + tier));

        if (inventory.getBalance() <= 0) {
            throw new RuntimeException("Đã hết lượt sử dụng gói " + tier);
        }

        inventory.setBalance(inventory.getBalance() - 1);
        inventoryRepository.save(inventory);
    }

    /**
     * Logic truy vấn dùng chung cho cả việc nạp tiền và kiểm tra lượt
     */
    private Optional<Inventory> findInventory(String userId, PackageType type, PackageTier tier) {
        return inventoryRepository.findByUserIdAndTypeAndTier(userId, type, tier);
    }

    /**
     * Logic cộng dồn số dư lượt dùng, xử lý trường hợp balance bị null
     */
    private void accumulateBalance(Inventory inventory, Integer quota) {
        int currentBalance = (inventory.getBalance() != null) ? inventory.getBalance() : 0;
        inventory.setBalance(currentBalance + quota);
    }

    /**
     * Kiểm tra nhanh xem kho đã hết lượt hay chưa
     */
    private boolean isBalanceEmpty(Inventory inventory) {
        return inventory.getBalance() == null || inventory.getBalance() <= 0;
    }

    /**
     * Khởi tạo đối tượng Inventory mới (chưa lưu DB)
     */
    private Inventory buildInventory(String userId, Packages pkg) {
        return Inventory.builder()
                .userId(userId)
                .type(pkg.getType())
                .tier(pkg.getTier())
                .balance(0)
                .build();
    }
}