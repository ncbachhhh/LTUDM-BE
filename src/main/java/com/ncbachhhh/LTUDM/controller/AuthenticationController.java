package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.AuthenticationRequest;
import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.ForgotPasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.LogoutRequest;
import com.ncbachhhh.LTUDM.dto.request.RefreshTokenRequest;
import com.ncbachhhh.LTUDM.dto.request.ResetPasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.request.VerifyResetOtpRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.AuthenticationResponse;
import com.ncbachhhh.LTUDM.dto.response.ResetOtpVerificationResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.service.AuthenticationService;
import com.ncbachhhh.LTUDM.service.PasswordResetService;
import com.ncbachhhh.LTUDM.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;
    PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ApiResponse.success(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.success(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.message("Logout successful.");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ApiResponse.message("If the email exists, an OTP has been sent.");
    }

    @PostMapping("/verify-reset-otp")
    public ApiResponse<ResetOtpVerificationResponse> verifyResetOtp(@RequestBody @Valid VerifyResetOtpRequest request) {
        return ApiResponse.success(
                passwordResetService.verifyResetOtp(request.getEmail(), request.getOtp()),
                "OTP verified successfully.");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ApiResponse.message("Password reset successfully.");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.message("Password changed successfully.");
    }
}
