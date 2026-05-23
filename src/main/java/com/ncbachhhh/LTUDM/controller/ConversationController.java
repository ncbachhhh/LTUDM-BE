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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    @GetMapping("/me")
    ApiResponse<List<ConversationResponse>> getMyConversations() {
        return success(conversationService.getMyConversations());
    }

    @GetMapping("/{conversationId}/info")
    ApiResponse<ConversationInfoResponse> getConversationInfo(@PathVariable String conversationId) {
        return success(conversationService.getConversationInfo(conversationId));
    }

    @PostMapping
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid CreateConversationRequest request) {
        return success(conversationService.createConversation(request));
    }

    @PostMapping("/{conversationId}/members")
    ApiResponse<ConversationResponse> addMembers(
            @PathVariable String conversationId,
            @RequestBody @Valid AddConversationMembersRequest request) {
        return success(conversationService.addMembers(conversationId, request));
    }

    @PatchMapping("/{conversationId}/members/{memberId}/nickname")
    ApiResponse<ConversationResponse> updateMemberNickname(
            @PathVariable String conversationId,
            @PathVariable String memberId,
            @RequestBody @Valid UpdateConversationNicknameRequest request) {
        return success(conversationService.updateMemberNickname(conversationId, memberId, request));
    }

    @DeleteMapping("/{conversationId}")
    ApiResponse<String> deleteGroupConversation(@PathVariable String conversationId) {
        conversationService.deleteGroupConversation(conversationId);
        return success("Group conversation deleted.");
    }

    private <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .data(data)
                .build();
    }
}
