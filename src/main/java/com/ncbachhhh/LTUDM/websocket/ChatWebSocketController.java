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
        String senderId = principal.getName();
        MessageResponse savedMessage = messageService.sendMessage(request, senderId);

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);
    }

    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId) {
        messageService.markAllAsRead(conversationId);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read", "Messages marked as read");
    }

    @MessageMapping("/chat/{conversationId}/typing")
    public void typing(
            @DestinationVariable String conversationId,
            @Payload TypingIndicator indicator) {

        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/typing", indicator);
    }

    public record TypingIndicator(String userId, String displayName, boolean isTyping) {
    }
}
