package com.ncbachhhh.LTUDM.config;

import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
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

import java.util.Date;
import java.util.List;

/**
 * Interceptor xác thực JWT token khi kết nối WebSocket.
 * Token được truyền qua header: Authorization: Bearer {token}
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

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

        return message;
    }
}
