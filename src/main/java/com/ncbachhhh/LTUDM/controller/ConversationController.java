package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.AddConversationMembersRequest;
import com.ncbachhhh.LTUDM.dto.request.CreateConversationRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.ConversationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // GET /conversations/me - Lấy danh sách conversation của user hiện tại (userId từ token)
    @GetMapping("/me")
    ApiResponse<List<ConversationResponse>> getMyConversations() {
        ApiResponse<List<ConversationResponse>> response = new ApiResponse<>();
        response.setData(conversationService.getMyConversations());
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // POST /conversations - Tạo đoạn chat cá nhân hoặc nhóm
    @PostMapping
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid CreateConversationRequest request) {
        ApiResponse<ConversationResponse> response = new ApiResponse<>();
        response.setData(conversationService.createConversation(request));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // POST /conversations/{conversationId}/members - Thêm thành viên vào nhóm chat
    @PostMapping("/{conversationId}/members")
    ApiResponse<ConversationResponse> addMembers(
            @PathVariable String conversationId,
            @RequestBody @Valid AddConversationMembersRequest request) {
        ApiResponse<ConversationResponse> response = new ApiResponse<>();
        response.setData(conversationService.addMembers(conversationId, request));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // DELETE /conversations/{conversationId} - Xóa nhóm chat
    @DeleteMapping("/{conversationId}")
    ApiResponse<String> deleteGroupConversation(@PathVariable String conversationId) {
        ApiResponse<String> response = new ApiResponse<>();
        conversationService.deleteGroupConversation(conversationId);
        response.setData("Group conversation deleted");
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }
}
