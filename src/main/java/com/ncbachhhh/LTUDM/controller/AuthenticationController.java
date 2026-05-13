package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.AuthenticationRequest;
import com.ncbachhhh.LTUDM.dto.request.ForgotPasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.IntrospectRequest;
import com.ncbachhhh.LTUDM.dto.request.LogoutRequest;
import com.ncbachhhh.LTUDM.dto.request.RefreshTokenRequest;
import com.ncbachhhh.LTUDM.dto.request.ResetPasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.VerifyResetOtpRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.AuthenticationResponse;
import com.ncbachhhh.LTUDM.dto.response.IntrospectResponse;
import com.ncbachhhh.LTUDM.dto.response.ResetOtpVerificationResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.AuthenticationService;
import com.ncbachhhh.LTUDM.service.PasswordResetService;
import com.ncbachhhh.LTUDM.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;
    PasswordResetService passwordResetService;

    // Đăng ký tài khoản mới
    // POST /auth/register - Đăng ký tài khoản mới
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.createUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // Đăng nhập
    // POST /auth/login - Đăng nhập và lấy token
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .data(result)
                .build();
    }

    // Refresh token - duy trì phiên đăng nhập
    // POST /auth/refresh - Làm mới cặp token
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .data(result)
                .build();
    }

    // Logout - hủy token
    // POST /auth/logout - Đăng xuất và thu hồi token
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đăng xuất thành công.")
                .build();
    }

    // Kiểm tra token
    // POST /auth/introspect - Kiểm tra token còn hợp lệ hay không
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .code(200)
                .data(result)
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Nếu email tồn tại, mã OTP đã được gửi.")
                .build();
    }

    @PostMapping("/verify-reset-otp")
    public ApiResponse<ResetOtpVerificationResponse> verifyResetOtp(@RequestBody @Valid VerifyResetOtpRequest request) {
        var result = passwordResetService.verifyResetOtp(request.getEmail(), request.getOtp());
        return ApiResponse.<ResetOtpVerificationResponse>builder()
                .code(200)
                .message("Xác thực OTP thành công.")
                .data(result)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đổi mật khẩu thành công.")
                .build();
    }

}
