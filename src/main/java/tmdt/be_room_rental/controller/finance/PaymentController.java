package tmdt.be_room_rental.controller.finance;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.service.interfaces.finance.IPaymentService;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/vnp/create/{orderId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ApiResponse<String> createPaymentUrl(@PathVariable String orderId, HttpServletRequest request) {

        String paymentUrl = paymentService.createPaymentUrl(orderId, request);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Tạo đường dẫn thanh toán thành công.")
                .data(paymentUrl)
                .build();
    }

    @GetMapping("/vnp/callback")
    @PreAuthorize("hasRole('LANDLORD')")
    public ApiResponse<Boolean> handlePaymentCallback(@RequestParam Map<String, String> params) {

        boolean isSuccess = paymentService.processPaymentCallback(params);
        String message = isSuccess ? "Thanh toán thành công." : "Thanh toán thất bại hoặc chữ ký không hợp lệ.";
        int code = isSuccess ? 200 : 400;
        return ApiResponse.<Boolean>builder()
                .code(code)
                .message(message)
                .data(isSuccess)
                .build();
    }
}