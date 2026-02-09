package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.AdminService;
import com.ncbachhhh.LTUDM.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    AdminService adminService;
    UserService userService;

    // POST /admin/users - Admin tạo user mới
    @PostMapping("/users")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRegisterRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(adminService.createUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // PUT /admin/users/{userId}/ban - Admin ban user
    @PutMapping("/users/{userId}/ban")
    ApiResponse<String> banUserById(@PathVariable("userId") String userId) {
        ApiResponse<String> response = new ApiResponse<>();
        userService.banUser(userId);
        response.setData("Successfully banned user with ID: " + userId);
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    // PUT /admin/users/{userId}/unban - Admin unban user
    @PutMapping("/users/{userId}/unban")
    ApiResponse<String> unbanUserById(@PathVariable("userId") String userId) {
        ApiResponse<String> response = new ApiResponse<>();
        userService.unbanUser(userId);
        response.setData("Successfully unbanned user with ID: " + userId);
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
