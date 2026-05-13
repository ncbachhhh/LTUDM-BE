package com.ncbachhhh.LTUDM.websocket;

import com.ncbachhhh.LTUDM.dto.request.MessageRequest;
import com.ncbachhhh.LTUDM.dto.response.MessageResponse;
import com.ncbachhhh.LTUDM.entity.Message.MessageType;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
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

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {
    MessageService messageService;
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
    }

    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId, Principal principal) {
        String userId = requireUserId(principal);
        messageService.markAllAsRead(conversationId, userId);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read", "Đã đánh dấu tin nhắn là đã đọc.");
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

    public record TypingIndicator(String userId, String displayName, boolean isTyping) {
    }
}
