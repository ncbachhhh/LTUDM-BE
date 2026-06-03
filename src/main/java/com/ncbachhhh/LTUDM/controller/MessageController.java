package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationLinkResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.ConversationService;
import com.ncbachhhh.LTUDM.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {
    MessageService messageService;
    ConversationService conversationService;
    SimpMessagingTemplate messagingTemplate;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<MessageResponse> sendMessageWithFile(
            @RequestPart("message") MessageRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        MessageResponse savedMessage = messageService.sendMessage(request, file);
        messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), savedMessage);
        sendConversationPreviewToMembers(request.getConversationId());
        return ApiResponse.success(savedMessage);
    }

    @GetMapping("/conversation/{conversationId}/paged")
    ApiResponse<Page<MessageResponse>> getMessagesPaged(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(messageService.getMessagesByConversationPaged(conversationId, page, size));
    }

    @PutMapping("/{messageId}/read")
    ApiResponse<String> markAsRead(@PathVariable String messageId) {
        messageService.markAsRead(messageId);
        return ApiResponse.success("Message marked as read.");
    }

    @PutMapping("/conversation/{conversationId}/read-all")
    ApiResponse<String> markAllAsRead(@PathVariable String conversationId) {
        messageService.markAllAsRead(conversationId);
        sendConversationPreviewToUser(conversationId, getCurrentUserId());
        return ApiResponse.success("All visible messages marked as read.");
    }

    @DeleteMapping("/{messageId}")
    ApiResponse<String> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return ApiResponse.success("Message deleted.");
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    ApiResponse<Long> countUnread(@PathVariable String conversationId) {
        return ApiResponse.success(messageService.countUnreadMessages(conversationId));
    }

    @GetMapping("/conversation/{conversationId}/latest")
    ApiResponse<MessageResponse> getLatestMessage(@PathVariable String conversationId) {
        return ApiResponse.success(messageService.getLatestMessage(conversationId));
    }

    @PutMapping("/{messageId}/pin")
    ApiResponse<MessageResponse> pinMessage(@PathVariable String messageId) {
        return ApiResponse.success(messageService.pinMessage(messageId));
    }

    @DeleteMapping("/{messageId}/pin")
    ApiResponse<String> unpinMessage(@PathVariable String messageId) {
        messageService.unpinMessage(messageId);
        return ApiResponse.success("Message unpinned.");
    }

    @GetMapping("/conversation/{conversationId}/pinned")
    ApiResponse<List<MessageResponse>> getPinnedMessages(@PathVariable String conversationId) {
        return ApiResponse.success(messageService.getPinnedMessages(conversationId));
    }

    @GetMapping("/conversation/{conversationId}/media/images")
    ApiResponse<Page<MessageResponse>> getConversationImages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(messageService.getConversationImages(conversationId, page, size));
    }

    @GetMapping("/conversation/{conversationId}/media/images/preview")
    ApiResponse<List<MessageResponse>> getConversationImagePreview(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.success(messageService.getConversationImagePreview(conversationId, limit));
    }

    @GetMapping("/conversation/{conversationId}/media/files")
    ApiResponse<Page<MessageResponse>> getConversationFiles(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(messageService.getConversationFiles(conversationId, page, size));
    }

    @GetMapping("/conversation/{conversationId}/media/links")
    ApiResponse<Page<ConversationLinkResponse>> getConversationLinks(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(messageService.getConversationLinks(conversationId, page, size));
    }

    private void sendConversationPreviewToMembers(String conversationId) {
        conversationService.getConversationMemberIds(conversationId)
                .forEach(memberId -> sendConversationPreviewToUser(conversationId, memberId));
    }

    private void sendConversationPreviewToUser(String conversationId, String userId) {
        ConversationResponse preview = conversationService.getConversationPreviewForUser(conversationId, userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/conversations", preview);
    }

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }

}
