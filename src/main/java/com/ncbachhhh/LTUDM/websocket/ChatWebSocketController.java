package com.ncbachhhh.LTUDM.websocket;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket Controller xử lý tin nhắn real-time.
 *
 * Flow gửi tin nhắn:
 * 1. Client gửi tin nhắn đến: /app/chat/{conversationId}
 * 2. Server lưu tin nhắn vào DB
 * 3. Server broadcast tin nhắn đến: /topic/conversation/{conversationId}
 * 4. Tất cả client subscribe conversation đó sẽ nhận được tin nhắn
 */
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {
    MessageService messageService;
    SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý tin nhắn gửi đến conversation
     * Client gửi đến: /app/chat/{conversationId}
     * Broadcast đến: /topic/conversation/{conversationId}
     */
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Payload MessageRequest request,
            Principal principal) {

        // Set conversation_id từ path
        request.setConversation_id(conversationId);

        // Lấy userId từ Principal (được set bởi WebSocketAuthInterceptor)
        String senderId = principal.getName();

        // Lưu tin nhắn vào DB
        MessageResponse savedMessage = messageService.sendMessage(request, senderId);

        // Broadcast tin nhắn đến tất cả người trong conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                savedMessage
        );
    }

    /**
     * Xử lý đánh dấu đã đọc
     * Client gửi đến: /app/chat/{conversationId}/read
     * Broadcast đến: /topic/conversation/{conversationId}/read
     */
    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId) {
        messageService.markAllAsRead(conversationId);

        // Thông báo cho các client khác biết tin nhắn đã được đọc
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/read",
                "Messages marked as read"
        );
    }

    /**
     * Thông báo đang gõ (typing indicator)
     * Client gửi đến: /app/chat/{conversationId}/typing
     * Broadcast đến: /topic/conversation/{conversationId}/typing
     */
    @MessageMapping("/chat/{conversationId}/typing")
    public void typing(
            @DestinationVariable String conversationId,
            @Payload TypingIndicator indicator) {

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/typing",
                indicator
        );
    }

    /**
     * DTO cho typing indicator
     */
    public record TypingIndicator(String userId, String displayName, boolean isTyping) {}
}
