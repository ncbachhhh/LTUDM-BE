package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.AddConversationMembersRequest;
import com.ncbachhhh.LTUDM.dto.request.CreateConversationRequest;
import com.ncbachhhh.LTUDM.dto.request.MuteConversationRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationEmojiRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationNicknameRequest;
import com.ncbachhhh.LTUDM.dto.request.UpdateConversationTitleRequest;
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

    // Lấy danh sách conversation của current user, gồm preview/latest/unread.
    @GetMapping("/me")
    ApiResponse<List<ConversationResponse>> getMyConversations() {
        return ApiResponse.success(conversationService.getMyConversations());
    }

    // Lấy thông tin chi tiết của một conversation để hiển thị info panel.
    @GetMapping("/{conversationId}/info")
    ApiResponse<ConversationInfoResponse> getConversationInfo(@PathVariable String conversationId) {
        return ApiResponse.success(conversationService.getConversationInfo(conversationId));
    }

    // Tạo direct conversation hoặc group conversation tuy theo request type.
    @PostMapping
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid CreateConversationRequest request) {
        return ApiResponse.success(conversationService.createConversation(request));
    }

    // Thêm member vào group conversation, chỉ owner/người có quyền quản lý mới được thực hiện.
    @PostMapping("/{conversationId}/members")
    ApiResponse<ConversationResponse> addMembers(
            @PathVariable String conversationId,
            @RequestBody @Valid AddConversationMembersRequest request) {
        return ApiResponse.success(conversationService.addMembers(conversationId, request));
    }

    // Cập nhật nickname của một member trong conversation.
    @PatchMapping("/{conversationId}/members/{memberId}/nickname")
    ApiResponse<ConversationResponse> updateMemberNickname(
            @PathVariable String conversationId,
            @PathVariable String memberId,
            @RequestBody @Valid UpdateConversationNicknameRequest request) {
        return ApiResponse.success(conversationService.updateMemberNickname(conversationId, memberId, request));
    }

    // Cập nhật emoji mặc định của conversation.
    @PatchMapping("/{conversationId}/emoji")
    ApiResponse<ConversationResponse> updateConversationEmoji(
            @PathVariable String conversationId,
            @RequestBody @Valid UpdateConversationEmojiRequest request) {
        return ApiResponse.success(conversationService.updateConversationEmoji(conversationId, request));
    }

    // Cập nhật title của group conversation.
    @PatchMapping("/{conversationId}/title")
    ApiResponse<ConversationResponse> updateConversationTitle(
            @PathVariable String conversationId,
            @RequestBody @Valid UpdateConversationTitleRequest request) {
        return ApiResponse.success(conversationService.updateConversationTitle(conversationId, request));
    }

    // Bật mute conversation cho current user với thời hạn trong request.
    @PatchMapping("/{conversationId}/mute")
    ApiResponse<ConversationResponse> muteConversation(
            @PathVariable String conversationId,
            @RequestBody @Valid MuteConversationRequest request) {
        return ApiResponse.success(conversationService.muteConversation(conversationId, request));
    }

    // Tắt mute conversation cho current user.
    @DeleteMapping("/{conversationId}/mute")
    ApiResponse<ConversationResponse> unmuteConversation(@PathVariable String conversationId) {
        return ApiResponse.success(conversationService.unmuteConversation(conversationId));
    }

    // Upload/cập nhật avatar cho group conversation.
    @PostMapping(value = "/{conversationId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ConversationResponse> updateGroupAvatar(
            @PathVariable String conversationId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(conversationService.updateGroupAvatar(conversationId, file));
    }

    // PUT avatar dùng chung logic với POST để FE có thể replace idempotent về mặt API.
    @PutMapping(value = "/{conversationId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<ConversationResponse> replaceGroupAvatar(
            @PathVariable String conversationId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(conversationService.updateGroupAvatar(conversationId, file));
    }

    // Xóa toàn bộ group conversation, chỉ áp dụng cho group và người có quyền quản lý.
    @DeleteMapping("/{conversationId}")
    ApiResponse<String> deleteGroupConversation(@PathVariable String conversationId) {
        conversationService.deleteGroupConversation(conversationId);
        return ApiResponse.success("Group conversation deleted.");
    }

    // An/xóa conversation chỉ riêng current user bằng conversation deletion records.
    @DeleteMapping("/{conversationId}/me")
    ApiResponse<String> deleteConversationForMe(@PathVariable String conversationId) {
        conversationService.deleteConversationForCurrentUser(conversationId);
        return ApiResponse.success("Conversation deleted for current user.");
    }

    // Current user rồi khỏi group conversation.
    @DeleteMapping("/{conversationId}/members/me")
    ApiResponse<ConversationResponse> leaveGroupConversation(@PathVariable String conversationId) {
        return ApiResponse.success(conversationService.leaveGroupConversation(conversationId));
    }

    // Owner/manager xóa một member khỏi group conversation.
    @DeleteMapping("/{conversationId}/members/{memberId}")
    ApiResponse<ConversationResponse> removeGroupMember(
            @PathVariable String conversationId,
            @PathVariable String memberId) {
        return ApiResponse.success(conversationService.removeGroupMember(conversationId, memberId));
    }

    // Chuyển quyền owner của group sang member khác.
    @PatchMapping("/{conversationId}/owner/{memberId}")
    ApiResponse<ConversationResponse> transferGroupOwnership(
            @PathVariable String conversationId,
            @PathVariable String memberId) {
        return ApiResponse.success(conversationService.transferGroupOwnership(conversationId, memberId));
    }
}
