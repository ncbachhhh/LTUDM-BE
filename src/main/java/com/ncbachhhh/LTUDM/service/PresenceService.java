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

    // Ghi nhan một WebSocket session đang online cho user; user chỉ publish online khi từ 0 len 1 session.
    public void markOnline(String sessionId, String userId) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userId)) {
            return;
        }

        // Nếu một session id bị gán sang user khác, gỡ khỏi user cũ trước khi them user mới.
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

    // Go session khi WebSocket disconnect; chỉ publish offline khi user không còn session nào.
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

    // Kiểm tra user có ít nhất một WebSocket session đang sống hay không.
    public boolean isOnline(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    // Xóa session khỏi user và trả về true nếu user vừa chuyển sang offline.
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

    // Broadcast presence event đến topic chung để client cập nhật online/offline realtime.
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
