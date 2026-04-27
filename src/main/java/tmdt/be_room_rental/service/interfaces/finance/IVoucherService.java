package tmdt.be_room_rental.service.interfaces.finance;

import tmdt.be_room_rental.dto.req.finance.VoucherRequest;
import tmdt.be_room_rental.dto.res.finance.VoucherResponse;

import java.util.List;

public interface IVoucherService {
    VoucherResponse createVoucher(VoucherRequest request);
    VoucherResponse updateVoucher(String id, VoucherRequest request);
    VoucherResponse updateActiveVoucher(String id, boolean isActive);
    VoucherResponse getVoucherById(String id);
    List<VoucherResponse> getVouchers();
    List<VoucherResponse> getActiveVouchers();
    void hiddenVoucher(String id);
    void deleteVoucher(String id);
}