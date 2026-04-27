package com.ncbachhhh.LTUDM.controller;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.ApiResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {
    MessageService messageService;
    SimpMessagingTemplate messagingTemplate;

    // POST /messages - Gửi tin nhắn mới
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        MessageResponse savedMessage = messageService.sendMessage(request);
        messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), savedMessage);
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setData(savedMessage);
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<MessageResponse> sendMessageWithImage(
            @RequestPart("message") MessageRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        MessageResponse savedMessage = messageService.sendMessage(request, file);
        messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), savedMessage);
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setData(savedMessage);
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // GET /messages/conversation/{conversationId} - Lấy tất cả tin nhắn trong conversation
    @GetMapping("/conversation/{conversationId}")
    ApiResponse<List<MessageResponse>> getMessagesByConversation(@PathVariable String conversationId) {
        ApiResponse<List<MessageResponse>> response = new ApiResponse<>();
        response.setData(messageService.getMessagesByConversation(conversationId));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // GET /messages/conversation/{conversationId}/paged - Lấy tin nhắn với phân trang
    @GetMapping("/conversation/{conversationId}/paged")
    ApiResponse<Page<MessageResponse>> getMessagesPaged(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ApiResponse<Page<MessageResponse>> response = new ApiResponse<>();
        response.setData(messageService.getMessagesByConversationPaged(conversationId, page, size));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // PUT /messages/{messageId}/read - Đánh dấu tin nhắn đã đọc
    @PutMapping("/{messageId}/read")
    ApiResponse<String> markAsRead(@PathVariable String messageId) {
        ApiResponse<String> response = new ApiResponse<>();
        messageService.markAsRead(messageId);
        response.setData("Message marked as read");
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // PUT /messages/conversation/{conversationId}/read-all - Đánh dấu tất cả tin nhắn đã đọc
    @PutMapping("/conversation/{conversationId}/read-all")
    ApiResponse<String> markAllAsRead(@PathVariable String conversationId) {
        ApiResponse<String> response = new ApiResponse<>();
        messageService.markAllAsRead(conversationId);
        response.setData("All messages marked as read");
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // DELETE /messages/{messageId} - Xóa tin nhắn (soft delete)
    @DeleteMapping("/{messageId}")
    ApiResponse<String> deleteMessage(@PathVariable String messageId) {
        ApiResponse<String> response = new ApiResponse<>();
        messageService.deleteMessage(messageId);
        response.setData("Message deleted");
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // GET /messages/conversation/{conversationId}/unread-count - Đếm tin nhắn chưa đọc
    @GetMapping("/conversation/{conversationId}/unread-count")
    ApiResponse<Long> countUnread(@PathVariable String conversationId) {
        ApiResponse<Long> response = new ApiResponse<>();
        response.setData(messageService.countUnreadMessages(conversationId));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    // GET /messages/conversation/{conversationId}/latest - Lấy tin nhắn mới nhất
    @GetMapping("/conversation/{conversationId}/latest")
    ApiResponse<MessageResponse> getLatestMessage(@PathVariable String conversationId) {
        ApiResponse<MessageResponse> response = new ApiResponse<>();
        response.setData(messageService.getLatestMessage(conversationId));
        response.setCode(ErrorCode.SUCCESS.getCode());
        return response;
    }
}
