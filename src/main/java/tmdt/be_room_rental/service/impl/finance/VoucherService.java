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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VoucherService implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public VoucherResponse createVoucher(VoucherRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime = request.getStartedAt().atStartOfDay();
        LocalDateTime expireDateTime = request.getExpiredAt().atTime(23, 59, 59);

        boolean isStarted = !request.getStartedAt().isAfter(today);
        boolean isNotExpired = !today.isAfter(request.getExpiredAt());
        boolean shouldBeActive = isStarted && isNotExpired;

        Voucher voucher = Voucher.builder()
                .discountPercentage(request.getDiscountPercentage())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .quantity(request.getQuantity())
                .createdAt(now)
                .startedAt(startDateTime)
                .expiredAt(expireDateTime)
                .usedByUsers(new ArrayList<>())
                .isActive(shouldBeActive)
                .usedCount(0)
                .build();

        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateVoucher(String id, VoucherRequest request) {
        Voucher voucher = findVoucherById(id);
        LocalDate today = LocalDate.now();

        if (request.getDiscountPercentage() != null) voucher.setDiscountPercentage(request.getDiscountPercentage());
        if (request.getMaxDiscountAmount() != null) voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getQuantity() != null) voucher.setQuantity(request.getQuantity());
        if (request.getStartedAt() != null) {
            voucher.setStartedAt(request.getStartedAt().atStartOfDay());
        }
        if (request.getExpiredAt() != null) {
            voucher.setExpiredAt(request.getExpiredAt().atTime(23, 59, 59));
        }

        LocalDate currentStart = voucher.getStartedAt().toLocalDate();
        LocalDate currentExpire = voucher.getExpiredAt().toLocalDate();

        boolean isStarted = !currentStart.isAfter(today);
        boolean isNotExpired = !today.isAfter(currentExpire);

        voucher.setIsActive(isStarted && isNotExpired);

        if (request.getIsActive() != null) {
            voucher.setIsActive(request.getIsActive());
        }

        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherResponse> getVouchers() {
        return voucherMapper.toResponseList(voucherRepository.findAll());
    }

    @Override
    public List<VoucherResponse> getActiveVouchers() {
        return voucherMapper.toResponseList(voucherRepository.findAllByIsActiveTrue());
    }

    @Override
    public VoucherResponse getVoucherById(String id) {
        return voucherMapper.toResponse(findVoucherById(id));
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
    public VoucherResponse updateActiveVoucher(String id, boolean isActive) {
        Voucher voucher = findVoucherById(id);
        voucher.setIsActive(isActive);
        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    private Voucher findVoucherById(String id) {
        return voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("ID Voucher không tồn tại"));
    }
}