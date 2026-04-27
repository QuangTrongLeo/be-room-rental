package tmdt.be_room_rental.service.impl.finance;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tmdt.be_room_rental.config.VNPayConfig;
import tmdt.be_room_rental.entity.Order;
import tmdt.be_room_rental.service.interfaces.finance.IPaymentService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final OrderService orderService;
    private final VNPayService vnPayService;
    private final VNPayConfig vnPayConfig;

    @Override
    public String createPaymentUrl(String orderId, HttpServletRequest httpRequest) {
        Order order = orderService.findOrderById(orderId);
        long amount = (long) (order.getTotalPrice() * 100);
        String vnp_TxnRef = order.getVnpTxnRef();
        String vnp_IpAddr = vnPayConfig.getIpAddress(httpRequest);
        return vnPayService.buildPaymentUrl(order.getId(), amount, vnp_TxnRef, vnp_IpAddr);
    }

    @Override
    public boolean processPaymentCallback(Map<String, String> params) {
        // 1. Verify chữ ký
        if (!vnPayService.verifyCallback(params)) {
            return false;
        }
        // 2. Lấy thông tin cơ bản
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        // 3. Xử lý trạng thái đơn hàng
        if ("00".equals(responseCode)) {
            orderService.successOrder(txnRef);
            return true;
        } else {
            orderService.failOrder(txnRef);
            return false;
        }
    }
}
