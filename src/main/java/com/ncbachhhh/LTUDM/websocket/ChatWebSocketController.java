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

    @MessageMapping("/chat/{conversationId}")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Payload MessageRequest request,
            Principal principal) {

        request.setConversationId(conversationId);
        if (request.getType() == MessageType.IMAGE) {
            throw new AppException(ErrorCode.IMAGE_MESSAGE_NOT_SUPPORTED_OVER_WEBSOCKET);
        }
        String senderId = requireUserId(principal);
        MessageResponse savedMessage = messageService.sendMessage(request, senderId);

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);
        sendConversationPreviewToMembers(conversationId);
    }

    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId, Principal principal) {
        String userId = requireUserId(principal);
        List<MessageReceipt> receipts = messageService.markAllAsRead(conversationId, userId);
        sendConversationPreviewToUser(conversationId, userId);
        publishReadEvent(conversationId, receipts);
    }

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

    private String requireUserId(Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return principal.getName();
    }

    private void sendConversationPreviewToMembers(String conversationId) {
        conversationService.getConversationMemberIds(conversationId)
                .forEach(memberId -> sendConversationPreviewToUser(conversationId, memberId));
    }

    private void sendConversationPreviewToUser(String conversationId, String userId) {
        ConversationResponse preview = conversationService.getConversationPreviewForUser(conversationId, userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/conversations", preview);
    }

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

    public record TypingIndicator(String userId, String displayName, boolean isTyping) {
    }
}
