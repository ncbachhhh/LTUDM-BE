package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.ChangePasswordRequest;
import com.ncbachhhh.LTUDM.dto.request.UserProfileUpdateRequest;
import com.ncbachhhh.LTUDM.dto.request.UserSettingsUpdateRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    // Cập nhật user theo id; chỉ owner của user do mới được phep sua.
    @PatchMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwner(authentication, #userId)")
    ApiResponse<UserResponse> updateUser(
            @PathVariable("userId") String userId,
            @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.success(userService.updateUser(userId, request));
    }

    // Lấy thông tin user hiện tại từ JWT trong SecurityContext.
    @GetMapping("/me")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.success(userService.getMyInfo());
    }

    // Cập nhật một phần profile của user hiện tại.
    @PatchMapping("/profile")
    ApiResponse<UserResponse> updateMyProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateMyProfile(request));
    }

    // PUT profile hiện đang dùng chung logic với PATCH để giữ API tương thích với FE.
    @PutMapping("/profile")
    ApiResponse<UserResponse> replaceMyProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateMyProfile(request));
    }

    // Cập nhật tuy chọn cả nhan như notification, theme, sound.
    @PatchMapping("/settings")
    ApiResponse<UserResponse> updateMySettings(@RequestBody @Valid UserSettingsUpdateRequest request) {
        return ApiResponse.success(userService.updateMySettings(request));
    }

    // PUT settings hiện đang dùng chung logic với PATCH.
    @PutMapping("/settings")
    ApiResponse<UserResponse> replaceMySettings(@RequestBody @Valid UserSettingsUpdateRequest request) {
        return ApiResponse.success(userService.updateMySettings(request));
    }

    // Tìm user bằng email và kèm theo relationship state với current user.
    @GetMapping("/search-by-email")
    ApiResponse<UserProfileResponse> searchUserByEmail(@RequestParam("email") String email) {
        return ApiResponse.success(userService.searchUserByEmail(email));
    }

    // Tìm user theo keyword để hiển thị trong luồng kết bạn.
    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchUsersForFriendRequest(@RequestParam("keyword") String keyword) {
        return ApiResponse.success(userService.searchUsersForFriendRequest(keyword));
    }

    // Lấy profile của user khác, có bổ sung trạng thái bạn bè/block.
    @GetMapping("/{userId}/profile")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("userId") String userId) {
        return ApiResponse.success(userService.getUserProfile(userId));
    }

    // Cập nhật avatar và trả về đầy đủ UserResponse cho màn hình profile.
    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<UserResponse> updateMyAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateMyAvatar(file));
    }

    // Upload avatar profile và trả về URL mới.
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<String> updateMyProfileAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateMyProfileAvatar(file));
    }

    // Thay avatar profile, dùng chung service với POST để tránh lap logic upload.
    @PutMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<String> replaceMyProfileAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateMyProfileAvatar(file));
    }

    // Upload ảnh nền profile và trả về URL mới.
    @PostMapping(value = "/profile/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<String> updateMyProfileBackground(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateMyProfileBackground(file));
    }

    // Thay ảnh nền profile, dùng chung service với POST.
    @PutMapping(value = "/profile/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<String> replaceMyProfileBackground(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateMyProfileBackground(file));
    }

    // Đổi mật khẩu của user đang đăng nhập.
    @PostMapping("/me/change-password")
    ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("Password changed successfully.");
    }

    // Vô hiệu hóa tài khoản hiện tại theo logic trong UserService.
    @DeleteMapping("/me")
    ApiResponse<String> deleteMyAccount() {
        userService.deleteMyAccount();
        return ApiResponse.success("Account deleted successfully.");
    }
}
