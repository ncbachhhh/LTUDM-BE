package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.AddConversationMembersRequest;
import com.ncbachhhh.LTUDM.dto.request.CreateConversationRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationNicknameRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationInfoResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.service.ConversationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    @GetMapping("/me")
    ApiResponse<List<ConversationResponse>> getMyConversations() {
        return ApiResponse.success(conversationService.getMyConversations());
    }

    @GetMapping("/{conversationId}/info")
    ApiResponse<ConversationInfoResponse> getConversationInfo(@PathVariable String conversationId) {
        return ApiResponse.success(conversationService.getConversationInfo(conversationId));
    }

    @PostMapping
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid CreateConversationRequest request) {
        return ApiResponse.success(conversationService.createConversation(request));
    }

    @PostMapping("/{conversationId}/members")
    ApiResponse<ConversationResponse> addMembers(
            @PathVariable String conversationId,
            @RequestBody @Valid AddConversationMembersRequest request) {
        return ApiResponse.success(conversationService.addMembers(conversationId, request));
    }

    @PatchMapping("/{conversationId}/members/{memberId}/nickname")
    ApiResponse<ConversationResponse> updateMemberNickname(
            @PathVariable String conversationId,
            @PathVariable String memberId,
            @RequestBody @Valid UpdateConversationNicknameRequest request) {
        return ApiResponse.success(conversationService.updateMemberNickname(conversationId, memberId, request));
    }

    @PostMapping(value = "/{conversationId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ConversationResponse> updateGroupAvatar(
            @PathVariable String conversationId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(conversationService.updateGroupAvatar(conversationId, file));
    }

    @PutMapping(value = "/{conversationId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ConversationResponse> replaceGroupAvatar(
            @PathVariable String conversationId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(conversationService.updateGroupAvatar(conversationId, file));
    }

    @DeleteMapping("/{conversationId}")
    ApiResponse<String> deleteGroupConversation(@PathVariable String conversationId) {
        conversationService.deleteGroupConversation(conversationId);
        return ApiResponse.success("Group conversation deleted.");
    }
}
