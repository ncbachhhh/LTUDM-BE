package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.FriendshipResponse;
import com.ncbachhhh.LTUDM.dto.response.UserProfileResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.FriendshipService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
        return ApiResponse.<FriendshipResponse>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.sendRequest(userId))
                .build();
    }

    @PostMapping("/{friendshipId}/accept")
    ApiResponse<FriendshipResponse> acceptRequest(@PathVariable String friendshipId) {
        return ApiResponse.<FriendshipResponse>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.acceptRequest(friendshipId))
                .build();
    }

    @PostMapping("/{friendshipId}/decline")
    ApiResponse<FriendshipResponse> declineRequest(@PathVariable String friendshipId) {
        return ApiResponse.<FriendshipResponse>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.declineRequest(friendshipId))
                .build();
    }

    @GetMapping("/requests/incoming")
    ApiResponse<List<FriendshipResponse>> getIncomingRequests() {
        return ApiResponse.<List<FriendshipResponse>>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.getIncomingRequests())
                .build();
    }

    @GetMapping("/requests/outgoing")
    ApiResponse<List<FriendshipResponse>> getOutgoingRequests() {
        return ApiResponse.<List<FriendshipResponse>>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.getOutgoingRequests())
                .build();
    }

    @GetMapping
    ApiResponse<List<FriendshipResponse>> getFriends() {
        return ApiResponse.<List<FriendshipResponse>>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.getFriends())
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<UserProfileResponse>> searchMyFriendsByName(@RequestParam("name") String name) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(friendshipService.searchMyFriendsByName(name))
                .build();
    }
}
