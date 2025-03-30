package me.experiments.websockettest.rate.limit;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static me.experiments.websockettest.config.Constants.MAX_SESSION_COUNT;

@Component
public class RateLimitingInterceptor implements HandshakeInterceptor {

    AtomicInteger activeSessionCount = new AtomicInteger();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (activeSessionCount.incrementAndGet() > MAX_SESSION_COUNT) {
            activeSessionCount.decrementAndGet();
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}

    public void afterConnectionClosed() {
        activeSessionCount.decrementAndGet();
    }
}
