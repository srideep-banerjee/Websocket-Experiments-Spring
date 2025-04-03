package me.experiments.websockettest.sockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class SocketConfig implements WebSocketConfigurer {

    private final SocketConnectionHandler connectionHandler;
    private final RateLimitingInterceptor interceptor;

    public SocketConfig(SocketConnectionHandler connectionHandler, RateLimitingInterceptor interceptor) {
        this.connectionHandler = connectionHandler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(connectionHandler, "/io")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
