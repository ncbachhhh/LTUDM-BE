package com.ncbachhhh.LTUDM.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Cấu hình WebSocket với STOMP protocol cho realtime chat.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}")
    private List<String> allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho message từ server gửi về client
        // Client subscribe: /topic/... hoặc /queue/...
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho message từ client gửi lên server
        // Client send: /app/...
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho message gửi đến user cụ thể
        // Server send: /user/{userId}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để client kết nối WebSocket
        // URL: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.toArray(String[]::new))
                .withSockJS()
                .setSessionCookieNeeded(false); // JWT auth does not need SockJS cookies/credentials.
    }
}
