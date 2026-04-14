package tmdt.be_room_rental.mapper.finance;

import org.springframework.stereotype.Component;
import tmdt.be_room_rental.dto.res.finance.VoucherResponse;
import tmdt.be_room_rental.entity.Voucher;

import java.util.List;

@Component
public class VoucherMapper {
    public VoucherResponse toResponse(Voucher voucher) {
        if (voucher == null) return null;
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountPercentage(voucher.getDiscountPercentage())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .quantity(voucher.getQuantity())
                .usedCount(voucher.getUsedCount())
                .type(voucher.getType())
                .createdAt(voucher.getCreatedAt())
                .expiredAt(voucher.getExpiredAt())
                .isActive(voucher.getIsActive())
                .build();
    }

    public List<VoucherResponse> toResponseList(List<Voucher> vouchers) {
        return vouchers.stream().map(this::toResponse).toList();
    }
}