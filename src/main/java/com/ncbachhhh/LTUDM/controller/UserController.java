package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    // PATCH /users/{userId} - Cập nhật thông tin user (chính chủ hoặc admin)
    @PatchMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.updateUser(userId, request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // GET /users/me - Lấy thông tin user hiện tại
    @GetMapping("/me")
    ApiResponse<UserResponse> getMyInfo() {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.getMyInfo());
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // POST /users/me/change-password - Đổi mật khẩu user hiện tại
    @PostMapping("/me/change-password")
    ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        ApiResponse<String> response = new ApiResponse<>();
        userService.changePassword(request);
        response.setData("Password changed successfully");
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // GET /users/{userId} - Lấy thông tin user theo ID (chính chủ hoặc admin)
    @GetMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.getUserById(userId));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
