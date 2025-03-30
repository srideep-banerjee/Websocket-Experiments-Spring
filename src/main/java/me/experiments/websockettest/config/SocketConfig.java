package me.experiments.websockettest.config;

import me.experiments.websockettest.handlers.SocketConnectionHandler;
import me.experiments.websockettest.rate.limit.RateLimitingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
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
