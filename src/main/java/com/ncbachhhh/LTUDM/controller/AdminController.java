package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {
    AdminService adminService;

    @PostMapping("/admin/auth/register")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRegisterRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(adminService.createUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
