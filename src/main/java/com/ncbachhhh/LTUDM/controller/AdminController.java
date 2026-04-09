package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.service.AdminService;
import com.ncbachhhh.LTUDM.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    AdminService adminService;
    UserService userService;

//    @PostMapping("/users")
//    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRegisterRequest request) {
//        ApiResponse<UserResponse> response = new ApiResponse<>();
//        response.setData(adminService.createUser(request));
//        response.setCode(ErrorCode.SUCCESS.getCode());
//        return response;
//    }
//
//    @PutMapping("/users/{userId}/ban")
//    ApiResponse<String> banUserById(@PathVariable("userId") String userId) {
//        ApiResponse<String> response = new ApiResponse<>();
//        userService.banUser(userId);
//        response.setData("Successfully banned user with ID: " + userId);
//        response.setCode(ErrorCode.SUCCESS.getCode());
//        return response;
//    }
//
//    @PutMapping("/users/{userId}/unban")
//    ApiResponse<String> unbanUserById(@PathVariable("userId") String userId) {
//        ApiResponse<String> response = new ApiResponse<>();
//        userService.unbanUser(userId);
//        response.setData("Successfully unbanned user with ID: " + userId);
//        response.setCode(ErrorCode.SUCCESS.getCode());
//        return response;
//    }
}
