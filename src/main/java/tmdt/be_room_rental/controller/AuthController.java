package tmdt.be_room_rental.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tmdt.be_room_rental.dto.req.auth.LoginRequest;
import tmdt.be_room_rental.dto.req.auth.RegisterRequest;
import tmdt.be_room_rental.dto.req.auth.VerifyOtpRequest;
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