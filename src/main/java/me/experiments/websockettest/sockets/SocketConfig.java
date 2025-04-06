package me.experiments.websockettest.sockets;

import me.experiments.websockettest.rooms.RoomInterceptor;
import org.springframework.beans.factory.annotation.Value;
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
    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final RoomInterceptor roomInterceptor;
    private final String path;

    public SocketConfig(
            SocketConnectionHandler connectionHandler,
            RateLimitingInterceptor rateLimitingInterceptor,
            RoomInterceptor roomInterceptor,
            @Value("${websocket.path}") String path) {
        this.connectionHandler = connectionHandler;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
        this.roomInterceptor = roomInterceptor;
        this.path = path;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(connectionHandler, path)
                .addInterceptors(roomInterceptor, rateLimitingInterceptor)
                .setAllowedOrigins("*");
    }
}
