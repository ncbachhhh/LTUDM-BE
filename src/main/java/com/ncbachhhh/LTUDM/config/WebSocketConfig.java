package com.ncbachhhh.LTUDM.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final String DEFAULT_CORS_ALLOWED_ORIGIN_PATTERNS =
            "${CORS_ALLOWED_ORIGIN_PATTERNS:http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*}";

    @Value(DEFAULT_CORS_ALLOWED_ORIGIN_PATTERNS)
    private List<String> allowedOriginPatterns;

    // Khai bao broker prefix: client send vào /app, client subscribe /topic hoặc /user /queue.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // Đăng ký SockJS endpoint /ws và cho phép origin theo cấu hình CORS pattern.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new))
                .withSockJS()
                .setSessionCookieNeeded(false);
    }
}
