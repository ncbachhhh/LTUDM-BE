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

    @PostMapping("/requests/{userId}")
    ApiResponse<FriendshipResponse> sendRequest(@PathVariable String userId) {
        return ApiResponse.success(friendshipService.sendRequest(userId));
    }

    @PostMapping("/{friendshipId}/accept")
    ApiResponse<FriendshipResponse> acceptRequest(@PathVariable String friendshipId) {
        return ApiResponse.success(friendshipService.acceptRequest(friendshipId));
    }

    @PostMapping("/{friendshipId}/decline")
    ApiResponse<FriendshipResponse> declineRequest(@PathVariable String friendshipId) {
        return ApiResponse.success(friendshipService.declineRequest(friendshipId));
    }

    @DeleteMapping("/requests/{friendshipId}")
    ApiResponse<String> withdrawRequest(@PathVariable String friendshipId) {
        friendshipService.withdrawRequest(friendshipId);
        return ApiResponse.success("Friendship request withdrawn.");
    }

    @DeleteMapping("/{friendshipId}")
    ApiResponse<String> deleteFriend(@PathVariable String friendshipId) {
        friendshipService.deleteFriend(friendshipId);
        return ApiResponse.success("Friend deleted.");
    }

    @PostMapping("/blocks/{userId}")
    ApiResponse<FriendshipResponse> blockUser(@PathVariable String userId) {
        return ApiResponse.success(friendshipService.blockUser(userId));
    }

    @DeleteMapping("/blocks/{userId}")
    ApiResponse<String> unblockUser(@PathVariable String userId) {
        friendshipService.unblockUser(userId);
        return ApiResponse.success("User unblocked.");
    }

    @GetMapping("/blocks")
    ApiResponse<List<FriendshipResponse>> getBlockedUsers() {
        return ApiResponse.success(friendshipService.getBlockedUsers());
    }

    @GetMapping("/requests/incoming")
    ApiResponse<List<FriendshipResponse>> getIncomingRequests() {
        return ApiResponse.success(friendshipService.getIncomingRequests());
    }

    @GetMapping("/requests/outgoing")
    ApiResponse<List<FriendshipResponse>> getOutgoingRequests() {
        return ApiResponse.success(friendshipService.getOutgoingRequests());
    }

    @GetMapping
    ApiResponse<List<FriendshipResponse>> getFriends() {
        return ApiResponse.success(friendshipService.getFriends());
    }

    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchMyFriendsByName(@RequestParam("name") String name) {
        return ApiResponse.success(friendshipService.searchMyFriendsByName(name));
    }
}
