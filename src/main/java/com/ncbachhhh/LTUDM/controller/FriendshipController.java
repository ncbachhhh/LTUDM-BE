package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.FriendshipResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.service.FriendshipService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendshipController {
    FriendshipService friendshipService;

    // Gửi lời mời kết bạn đến user được chỉ dinh.
    @PostMapping("/requests/{userId}")
    ApiResponse<FriendshipResponse> sendRequest(@PathVariable String userId) {
        return ApiResponse.success(friendshipService.sendRequest(userId));
    }

    // Chấp nhan lời mời kết bạn và tạo/tìm direct conversation cho hai user.
    @PostMapping("/{friendshipId}/accept")
    ApiResponse<FriendshipResponse> acceptRequest(@PathVariable String friendshipId) {
        return ApiResponse.success(friendshipService.acceptRequest(friendshipId));
    }

    // Tu chồi lời mời kết bạn đang cho.
    @PostMapping("/{friendshipId}/decline")
    ApiResponse<FriendshipResponse> declineRequest(@PathVariable String friendshipId) {
        return ApiResponse.success(friendshipService.declineRequest(friendshipId));
    }

    // Huy lời mời đã gửi đi khi nó vẫn ở trạng thái pending.
    @DeleteMapping("/requests/{friendshipId}")
    ApiResponse<String> withdrawRequest(@PathVariable String friendshipId) {
        friendshipService.withdrawRequest(friendshipId);
        return ApiResponse.success("Friendship request withdrawn.");
    }

    // Xóa quan hệ bạn bè hiện tại.
    @DeleteMapping("/{friendshipId}")
    ApiResponse<String> deleteFriend(@PathVariable String friendshipId) {
        friendshipService.deleteFriend(friendshipId);
        return ApiResponse.success("Friend deleted.");
    }

    // Chặn user khác; block được ưu tiên hon mới trạng thái friendship.
    @PostMapping("/blocks/{userId}")
    ApiResponse<FriendshipResponse> blockUser(@PathVariable String userId) {
        return ApiResponse.success(friendshipService.blockUser(userId));
    }

    // Bỏ chặn user đã bị current user block.
    @DeleteMapping("/blocks/{userId}")
    ApiResponse<String> unblockUser(@PathVariable String userId) {
        friendshipService.unblockUser(userId);
        return ApiResponse.success("User unblocked.");
    }

    // Lấy danh sách user mà current user đang chặn.
    @GetMapping("/blocks")
    ApiResponse<List<FriendshipResponse>> getBlockedUsers() {
        return ApiResponse.success(friendshipService.getBlockedUsers());
    }

    // Lấy các lời mời kết bạn gửi đến current user.
    @GetMapping("/requests/incoming")
    ApiResponse<List<FriendshipResponse>> getIncomingRequests() {
        return ApiResponse.success(friendshipService.getIncomingRequests());
    }

    // Lấy các lời mời kết bạn current user đã gửi đi.
    @GetMapping("/requests/outgoing")
    ApiResponse<List<FriendshipResponse>> getOutgoingRequests() {
        return ApiResponse.success(friendshipService.getOutgoingRequests());
    }

    // Lấy danh sách bạn bè đã accepted.
    @GetMapping
    ApiResponse<List<FriendshipResponse>> getFriends() {
        return ApiResponse.success(friendshipService.getFriends());
    }

    // Tìm trong danh sách bạn bè của current user theo tên.
    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchMyFriendsByName(@RequestParam("name") String name) {
        return ApiResponse.success(friendshipService.searchMyFriendsByName(name));
    }
}
