package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserUpdateRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.dto.response.UserResponse;
import com.ncbachhhh.LTUDM.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PatchMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
    ApiResponse<UserResponse> updateUser(
            @PathVariable("userId") String userId,
            @RequestBody @Valid UserUpdateRequest request) {
        return success(userService.updateUser(userId, request));
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getMyInfo() {
        return success(userService.getMyInfo());
    }

    @GetMapping("/search-by-email")
    ApiResponse<UserProfileResponse> searchUserByEmail(@RequestParam("email") String email) {
        return success(userService.searchUserByEmail(email));
    }

    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchUsersForFriendRequest(@RequestParam("keyword") String keyword) {
        return success(userService.searchUsersForFriendRequest(keyword));
    }

    @GetMapping("/{userId}/profile")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("userId") String userId) {
        return success(userService.getUserProfile(userId));
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<UserResponse> updateMyAvatar(@RequestParam("file") MultipartFile file) {
        return success(userService.updateMyAvatar(file));
    }

    @PostMapping("/me/change-password")
    ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return success("Password changed successfully.");
    }

    private <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .data(data)
                .build();
    }
}
