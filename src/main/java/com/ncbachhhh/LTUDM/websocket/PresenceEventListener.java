package com.ncbachhhh.LTUDM.websocket;

import com.ncbachhhh.LTUDM.service.PresenceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PresenceEventListener {
    PresenceService presenceService;

    // Khi STOMP session connected, mark online nếu event đã có Principal hợp lệ.
    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user == null) {
            return;
        }
        presenceService.markOnline(resolveSessionId(event), user.getName());
    }

    // Khi session disconnect, gỡ session khỏi PresenceService.
    @EventListener
    public void handleDisconnected(SessionDisconnectEvent event) {
        presenceService.markOffline(event.getSessionId());
    }

    // Lấy simpSessionId từ headers của connected event.
    private String resolveSessionId(SessionConnectedEvent event) {
        Object sessionId = event.getMessage().getHeaders().get("simpSessionId");
        return sessionId == null ? null : sessionId.toString();
    }
}
