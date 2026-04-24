package tmdt.be_room_rental.service.interfaces.finance;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IPaymentService {
    String createPaymentUrl(String orderId, HttpServletRequest httpRequest);
    boolean processPaymentCallback(Map<String, String> params);
}
