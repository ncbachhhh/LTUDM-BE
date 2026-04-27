package com.ncbachhhh.LTUDM.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket với STOMP protocol cho realtime chat.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Cho phep dung ca native WebSocket va SockJS fallback
    }
}
