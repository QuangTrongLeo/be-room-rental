package tmdt.be_room_rental.service.impl.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.dto.req.finance.VoucherRequest;
import tmdt.be_room_rental.dto.res.finance.VoucherResponse;
import tmdt.be_room_rental.entity.Voucher;
import tmdt.be_room_rental.entity.User;
import tmdt.be_room_rental.enums.type.VoucherType;
import tmdt.be_room_rental.mapper.finance.VoucherMapper;
import tmdt.be_room_rental.repository.finance.VoucherRepository;
import tmdt.be_room_rental.service.impl.auth.SecurityService;
import tmdt.be_room_rental.service.interfaces.finance.IVoucherService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VoucherService implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final SecurityService securityService;

    @Override
    public VoucherResponse createVoucher(VoucherRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = (request.getType() == VoucherType.MONTH)
                ? now.plusMonths(1)
                : now.plusWeeks(1);

        String finalCode = (request.getCode() == null || request.getCode().isBlank())
                ? generateUniqueCode(request.getType())
                : request.getCode().toUpperCase();

        if (voucherRepository.existsByCode(finalCode)) {
            throw new RuntimeException("Mã Voucher đã tồn tại: " + finalCode);
        }

        Voucher voucher = Voucher.builder()
                .code(finalCode)
                .discountPercentage(request.getDiscountPercentage())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .quantity(request.getQuantity())
                .type(request.getType())
                .createdAt(now)
                .expiredAt(expiredAt)
                .usedByUsers(new ArrayList<>())
                .isActive(true)
                .usedCount(0)
                .build();

        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateVoucher(String id, VoucherRequest request) {
        Voucher voucher = findVoucherById(id);
        if (request.getDiscountPercentage() != null) voucher.setDiscountPercentage(request.getDiscountPercentage());
        if (request.getMaxDiscountAmount() != null) voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getQuantity() != null) voucher.setQuantity(request.getQuantity());

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().toUpperCase();
            if (!newCode.equals(voucher.getCode()) && voucherRepository.existsByCode(newCode)) {
                throw new RuntimeException("Mã Voucher mới đã tồn tại");
            }
            voucher.setCode(newCode);
        }
        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherResponse> getVouchers() {
        return voucherMapper.toResponseList(voucherRepository.findAll());
    }

    @Override
    public VoucherResponse getVoucherById(String id) {
        return voucherMapper.toResponse(findVoucherById(id));
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
        return voucherMapper.toResponse(voucher);
    }

    @Override
    public void hiddenVoucher(String id) {
        Voucher voucher = findVoucherById(id);
        voucher.setIsActive(false);
        voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(String id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy Voucher để xóa vĩnh viễn");
        }
        voucherRepository.deleteById(id);
    }

    @Override
    public VoucherResponse toggleVoucherStatus(String id, boolean isActive) {
        Voucher voucher = findVoucherById(id);
        voucher.setIsActive(isActive);
        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public Double calculateDiscount(String code, Double totalAmount) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        String currentUserId = securityService.getCurrentUser().getId();

        if (!isValid(voucher, currentUserId)) {
            throw new RuntimeException("Voucher không khả dụng hoặc bạn đã sử dụng mã này");
        }

        double discount = totalAmount * (voucher.getDiscountPercentage() / 100.0);
        return Math.min(discount, voucher.getMaxDiscountAmount());
    }

    @Override
    public boolean isValidVoucher(String code) {
        // Tự động lấy User đang đăng nhập bên trong Service
        String currentUserId = securityService.getCurrentUser().getId();
        return voucherRepository.findByCode(code.toUpperCase())
                .map(v -> isValid(v, currentUserId))
                .orElse(false);
    }

    private String generateUniqueCode(VoucherType type) {
        String prefix = (type == VoucherType.MONTH) ? "MON" : "WEE";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String generatedCode;
        do {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
            generatedCode = sb.toString();
        } while (voucherRepository.existsByCode(generatedCode));
        return generatedCode;
    }

    private Voucher findVoucherById(String id) {
        return voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("ID Voucher không tồn tại"));
    }

    private boolean isValid(Voucher voucher, String userId) {
        return voucher.getIsActive()
                && voucher.getExpiredAt().isAfter(LocalDateTime.now())
                && voucher.getUsedCount() < voucher.getQuantity()
                && !voucher.getUsedByUsers().contains(userId);
    }
}