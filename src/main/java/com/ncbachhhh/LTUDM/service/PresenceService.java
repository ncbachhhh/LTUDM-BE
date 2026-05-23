package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.response.PresenceResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PresenceService {
    ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;
    ConcurrentHashMap<String, String> sessionUsers = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void markOnline(String sessionId, String userId) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userId)) {
            return;
        }

        String previousUserId = sessionUsers.put(sessionId, userId);
        if (previousUserId != null && !previousUserId.equals(userId)) {
            removeSession(previousUserId, sessionId);
        }

        Set<String> sessions = userSessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = sessions.isEmpty();
        sessions.add(sessionId);

        if (wasOffline) {
            publish(userId, true);
        }
    }

    public void markOffline(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }

        String userId = sessionUsers.remove(sessionId);
        if (!StringUtils.hasText(userId)) {
            return;
        }

        boolean offline = removeSession(userId, sessionId);
        if (offline) {
            publish(userId, false);
        }
    }

    public boolean isOnline(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    private boolean removeSession(String userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions == null) {
            return false;
        }

        sessions.remove(sessionId);
        if (!sessions.isEmpty()) {
            return false;
        }

        userSessions.remove(userId, sessions);
        return true;
    }

    private void publish(String userId, boolean online) {
        SimpMessagingTemplate messagingTemplate = messagingTemplateProvider.getIfAvailable();
        if (messagingTemplate == null) {
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/presence",
                PresenceResponse.builder()
                        .userId(userId)
                        .online(online)
                        .build()
        );
    }
}
