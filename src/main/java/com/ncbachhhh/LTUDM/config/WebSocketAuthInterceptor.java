package com.ncbachhhh.LTUDM.config;

import com.ncbachhhh.LTUDM.entity.Conversation.ConversationType;
import com.ncbachhhh.LTUDM.entity.ConversationMembers.ConversationMember;
import com.ncbachhhh.LTUDM.entity.Friendship.FriendshipStatus;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.BlockRepository;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.ConversationRepository;
import com.ncbachhhh.LTUDM.repository.FriendshipRepository;
import com.ncbachhhh.LTUDM.repository.InvalidatedTokenRepository;
import com.ncbachhhh.LTUDM.service.PresenceService;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JWT_SECRET_PROPERTY = "${jwt.secret}";
    private static final String SCOPE_CLAIM = "scope";

    private static final Pattern CONVERSATION_DESTINATION_PATTERN =
            Pattern.compile("^/(?:app/chat|topic/conversation)/([^/]+)(?:/.*)?$");

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationRepository conversationRepository;
    private final BlockRepository blockRepository;
    private final FriendshipRepository friendshipRepository;
    private final PresenceService presenceService;

    @Value(JWT_SECRET_PROPERTY)
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnection(accessor);
        }

        if (requiresDestinationAuthorization(command)) {
            authorizeDestinationAccess(accessor);
        }

        return message;
    }

    private void authenticateConnection(StompHeaderAccessor accessor) {
        try {
            SignedJWT signedJwt = parseAndValidateToken(extractBearerToken(accessor));
            String userId = signedJwt.getJWTClaimsSet().getSubject();
            String scope = (String) signedJwt.getJWTClaimsSet().getClaim(SCOPE_CLAIM);

            accessor.setUser(createAuthentication(userId, scope));
            presenceService.markOnline(accessor.getSessionId(), userId);

            log.info("WebSocket connected: userId={}", userId);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("WebSocket authentication failed", exception);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("WebSocket: No token provided");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return authHeader.substring(BEARER_PREFIX.length());
    }

    private SignedJWT parseAndValidateToken(String token) throws Exception {
        SignedJWT signedJwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

        if (!signedJwt.verify(verifier)) {
            log.warn("WebSocket: Invalid token signature");
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        Date expiration = signedJwt.getJWTClaimsSet().getExpirationTime();
        if (expiration == null || expiration.before(new Date())) {
            log.warn("WebSocket: Token expired");
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (invalidatedTokenRepository.existsById(signedJwt.getJWTClaimsSet().getJWTID())) {
            log.warn("WebSocket: Token revoked");
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return signedJwt;
    }

    private boolean requiresDestinationAuthorization(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command);
    }

    private UsernamePasswordAuthenticationToken createAuthentication(String userId, String scope) {
        List<SimpleGrantedAuthority> authorities = StringUtils.hasText(scope)
                ? List.of(new SimpleGrantedAuthority(scope))
                : Collections.emptyList();

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    private void authorizeDestinationAccess(StompHeaderAccessor accessor) {
        String conversationId = extractConversationId(accessor.getDestination());
        if (conversationId == null) {
            return;
        }

        String userId = getAuthenticatedUserId(accessor);
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            ensureDirectConversationIsBetweenFriends(conversationId, userId);
        }
    }

    private String extractConversationId(String destination) {
        if (!StringUtils.hasText(destination)) {
            return null;
        }

        var matcher = CONVERSATION_DESTINATION_PATTERN.matcher(destination);
        return matcher.matches() ? matcher.group(1) : null;
    }

    private String getAuthenticatedUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null || !StringUtils.hasText(accessor.getUser().getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return accessor.getUser().getName();
    }

    private void ensureDirectConversationIsBetweenFriends(String conversationId, String userId) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        if (conversation.getType() != ConversationType.DIRECT) {
            return;
        }

        String otherUserId = conversationMemberRepository.findByIdConversationId(conversationId).stream()
                .map(ConversationMember::getId)
                .map(id -> id.getUserId())
                .filter(memberId -> !memberId.equals(userId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_DIRECT_CONVERSATION_MEMBERS));

        if (blockRepository.existsByBlockerIdAndBlockedId(userId, otherUserId)
                || blockRepository.existsByBlockerIdAndBlockedId(otherUserId, userId)) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }

        if (!friendshipRepository.existsBetweenUsersByStatus(userId, otherUserId, FriendshipStatus.ACCEPTED)) {
            throw new AppException(ErrorCode.NOT_FRIENDS);
        }
    }
}
