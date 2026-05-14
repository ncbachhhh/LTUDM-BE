package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PatchMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.updateUser(userId, request));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getMyInfo() {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.getMyInfo());
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchUsers(@RequestParam("query") String query) {
        ApiResponse<List<UserProfileResponse>> response = new ApiResponse<>();
        response.setData(userService.searchUsers(query));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @GetMapping("/{userId}/profile")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("userId") String userId) {
        ApiResponse<UserProfileResponse> response = new ApiResponse<>();
        response.setData(userService.getUserProfile(userId));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<UserResponse> updateMyAvatar(@RequestParam("file") MultipartFile file) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.updateMyAvatar(file));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @PostMapping("/me/change-password")
    ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        ApiResponse<String> response = new ApiResponse<>();
        userService.changePassword(request);
        response.setData("Đổi mật khẩu thành công.");
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") String userId) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userService.getUserById(userId));
        response.setCode(ErrorCode.SUCCESS.getCode());

        return response;
    }
}
