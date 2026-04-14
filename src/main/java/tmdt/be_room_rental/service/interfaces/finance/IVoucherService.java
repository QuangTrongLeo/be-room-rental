package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.req.finance.VoucherRequest;
import tmdt.be_room_rental.dto.res.finance.VoucherResponse;

import java.util.List;

public interface IVoucherService {
    VoucherResponse createVoucher(VoucherRequest request);
    VoucherResponse updateVoucher(String id, VoucherRequest request);
    VoucherResponse getVoucherById(String id);
    VoucherResponse getVoucherByCode(String code);
    List<VoucherResponse> getVouchers();
    void hiddenVoucher(String id);
    void deleteVoucher(String id);

    // Kích hoạt hoặc tạm dừng hoạt động của Voucher (Admin)
    VoucherResponse toggleVoucherStatus(String id, boolean isActive);

    // Tính toán số tiền được giảm dựa trên mã code và tổng tiền đơn hàng
    Double calculateDiscount(String code, Double totalAmount);

    // Kiểm tra tính hợp lệ của Voucher (Còn hạn? Còn lượt dùng? User đã dùng chưa?)
    boolean isValidVoucher(String code);
}