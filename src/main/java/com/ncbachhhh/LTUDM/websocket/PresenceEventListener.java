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

    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user == null) {
            return;
        }
        presenceService.markOnline(resolveSessionId(event), user.getName());
    }

    @EventListener
    public void handleDisconnected(SessionDisconnectEvent event) {
        presenceService.markOffline(event.getSessionId());
    }

    private String resolveSessionId(SessionConnectedEvent event) {
        Object sessionId = event.getMessage().getHeaders().get("simpSessionId");
        return sessionId == null ? null : sessionId.toString();
    }
}
