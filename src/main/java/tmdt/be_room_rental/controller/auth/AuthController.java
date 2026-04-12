package tmdt.be_room_rental.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.auth.*;
import tmdt.be_room_rental.dto.res.ApiResponse;
import tmdt.be_room_rental.dto.res.auth.TokenResponse;
import tmdt.be_room_rental.service.interfaces.auth.IAuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Đăng ký thành công. Vui lòng kiểm tra email để lấy mã OTP.")
                .data(null)
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Xác thực tài khoản thành công.")
                .data(null)
                .build();
    }

    @PostMapping("/forget-password")
    public ApiResponse<Void> forgetPassword(@RequestBody ForgetPasswordRequest request) {
        authService.forgetPassword(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Mã OTP đặt lại mật khẩu đã được gửi.")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Mật khẩu đã được thay đổi thành công.")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ApiResponse.<TokenResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Đăng nhập thành công.")
                .data(tokenResponse)
                .build();
    }
}