package com.bzy.takeaway.config;

import com.bzy.takeaway.websocket.NotificationWebSocketHandler;
import com.bzy.takeaway.websocket.RiderWebSocketHandler;
import com.bzy.takeaway.websocket.WebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RiderWebSocketHandler riderWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(riderWebSocketHandler, "/ws/rider")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
        
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
