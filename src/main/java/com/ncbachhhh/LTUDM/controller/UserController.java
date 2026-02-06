package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.UserRegisterRequest;
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
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/auth/register")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRegisterRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.createUser(request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

//    @PostMapping("/auth/login")
//    ApiResponse<User> loginUser(@RequestBody @Valid UserLoginRequest request) {
//        ApiResponse<User> response = new ApiResponse<>();
//        response.setData(userService.loginUser(request));
//        response.setCode(ErrorCode.SUCCESS.getCode());
//
//        return response;
//    }

    @PostMapping("/user/update/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.updateUser(userId, request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.getUserById(userId));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId) or hasRole('ADMIN')")
    ApiResponse<String> deleteUserById(@PathVariable("userId") String userId) {
        ApiResponse<String> response = new ApiResponse<>();
        userService.deleteUser(userId);
        response.setData("Successfully deleted user with ID: " + userId);
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
