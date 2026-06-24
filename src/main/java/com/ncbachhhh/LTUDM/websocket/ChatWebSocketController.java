package com.ncbachhhh.LTUDM.websocket;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageReadEventResponse;
import com.ncbachhhh.LTUDM.dto.response.ConversationResponse;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.MessageReceipt.MessageReceipt;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.service.ConversationService;
import com.ncbachhhh.LTUDM.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {
    MessageService messageService;
    ConversationService conversationService;
    SimpMessagingTemplate messagingTemplate;

    // Nhận text message từ STOMP, lưu DB, rồi broadcast đến topic conversation và preview member.
    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Payload MessageRequest request,
            Principal principal) {

        request.setConversationId(conversationId);
        // Ảnh/file cần multipart REST để upload R2; WebSocket chỉ xử lý payload nhẹ.
        if (request.getType() == MessageType.IMAGE) {
            throw new AppException(ErrorCode.IMAGE_MESSAGE_NOT_SUPPORTED_OVER_WEBSOCKET);
        }
        String senderId = requireUserId(principal);
        MessageResponse savedMessage = messageService.sendMessage(request, senderId);

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);
        sendConversationPreviewToMembers(conversationId);
    }

    // Mark all visible messages as read qua socket và publish read event realtime.
    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId, Principal principal) {
        String userId = requireUserId(principal);
        List<MessageReceipt> receipts = messageService.markAllAsRead(conversationId, userId);
        sendConversationPreviewToUser(conversationId, userId);
        publishReadEvent(conversationId, receipts);
    }

    // Broadcast typing indicator sau khi xác thực user có quyền vào conversation.
    @MessageMapping("/chat/{conversationId}/typing")
    public void typing(
            @DestinationVariable String conversationId,
            @Payload TypingIndicator indicator,
            Principal principal) {
        String userId = requireUserId(principal);
        messageService.ensureCanAccessConversation(conversationId, userId);

        TypingIndicator sanitizedIndicator = new TypingIndicator(
                userId,
                StringUtils.hasText(indicator.displayName()) ? indicator.displayName() : null,
                indicator.isTyping()
        );

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/typing", sanitizedIndicator);
    }

    // Lấy user id từ Principal đã được WebSocketAuthInterceptor gán khi CONNECT.
    private String requireUserId(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getName();
    }

    // Gửi preview conversation mới cho tất cả member sau khi có message mới.
    private void sendConversationPreviewToMembers(String conversationId) {
        conversationService.getConversationMemberIds(conversationId)
                .forEach(memberId -> sendConversationPreviewToUser(conversationId, memberId));
    }

    // Gửi preview vào user queue riêng của một member.
    private void sendConversationPreviewToUser(String conversationId, String userId) {
        ConversationResponse preview = conversationService.getConversationPreviewForUser(conversationId, userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/conversations", preview);
    }

    // Tạo và broadcast event read receipt nếu có message mới được đánh dấu đã đọc.
    private void publishReadEvent(String conversationId, List<MessageReceipt> receipts) {
        if (receipts == null || receipts.isEmpty()) {
            return;
        }

        MessageReadEventResponse event = MessageReadEventResponse.builder()
                .eventType("MESSAGES_READ")
                .conversationId(conversationId)
                .reader(messageService.toSeenByResponse(receipts.getFirst(), conversationId))
                .messageIds(receipts.stream()
                        .map(receipt -> receipt.getId().getMessageId())
                        .toList())
                .occurredAt(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read", event);
    }

    // Payload typing indicator gửi qua STOMP; userId sẽ được server overwrite bằng Principal.
    public record TypingIndicator(String userId, String displayName, boolean isTyping) {
    }
}
