package com.ncbachhhh.LTUDM.config;

import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.ConversationMemberRepository;
import com.ncbachhhh.LTUDM.repository.InvalidatedTokenRepository;
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

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor xác thực JWT token khi kết nối WebSocket.
 * Token được truyền qua header: Authorization: Bearer {token}
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private static final Pattern CONVERSATION_DESTINATION_PATTERN =
            Pattern.compile("^/(?:app/chat|topic/conversation)/([^/]+)(?:/.*)?$");

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy token từ header
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket: No token provided");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            String token = authHeader.substring(7);

            try {
                // Verify token
                SignedJWT signedJWT = SignedJWT.parse(token);
                JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

                if (!signedJWT.verify(verifier)) {
                    log.warn("WebSocket: Invalid token signature");
                    throw new AppException(ErrorCode.INVALID_TOKEN);
                }

                // Kiểm tra token hết hạn
                Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
                if (expiration.before(new Date())) {
                    log.warn("WebSocket: Token expired");
                    throw new AppException(ErrorCode.INVALID_TOKEN);
                }
                if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
                    log.warn("WebSocket: Token revoked");
                    throw new AppException(ErrorCode.INVALID_TOKEN);
                }

                // Lấy thông tin user từ token
                String userId = signedJWT.getJWTClaimsSet().getSubject();
                String scope = (String) signedJWT.getJWTClaimsSet().getClaim("scope");

                // Tạo authentication
                var authorities = List.of(new SimpleGrantedAuthority(scope));
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                accessor.setUser(authentication);

                log.info("WebSocket connected: userId={}", userId);

            } catch (Exception e) {
                log.error("WebSocket authentication failed", e);
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
        }

        if (accessor != null
                && (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand()))) {
            ensureCanAccessDestination(accessor);
        }

        return message;
    }

    private void ensureCanAccessDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)) {
            return;
        }

        Matcher matcher = CONVERSATION_DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        if (accessor.getUser() == null || !StringUtils.hasText(accessor.getUser().getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String conversationId = matcher.group(1);
        String userId = accessor.getUser().getName();
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, userId)) {
            throw new AppException(ErrorCode.NOT_CONVERSATION_MEMBER);
        }
    }
}
