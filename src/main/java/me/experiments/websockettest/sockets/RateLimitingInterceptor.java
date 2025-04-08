package me.experiments.websockettest.sockets;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static me.experiments.websockettest.Constants.MAX_SESSION_PER_ROOM;
import static me.experiments.websockettest.Constants.MAX_MSG_PER_HOUR;

@Component
public class RateLimitingInterceptor implements HandshakeInterceptor {

    public static byte ACCEPT = 1;
    public static byte INFORMED_REJECT = 2;
    public static byte REJECT = 3;

    private final ConcurrentHashMap<String, Integer> activeSessionsPerRoom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> messageCount = new ConcurrentHashMap<>();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String roomId = (String) attributes.get("roomId");
        int sessionCount = activeSessionsPerRoom.compute(roomId, (room, count) -> count == null ? 1 : count + 1);
        if (sessionCount > MAX_SESSION_PER_ROOM) {
            activeSessionsPerRoom.compute(roomId, (room, count) -> --count);
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}

    public void afterConnectionClosed(WebSocketSession session) {
        String roomId = (String) session.getAttributes().get("roomId");
        activeSessionsPerRoom.compute(roomId, (room, count) -> --count);
    }

    public byte registerNewMessage(WebSocketSession session) {
        messageCount.putIfAbsent(session.getId(), new AtomicInteger());
        AtomicInteger msgCount = messageCount.get(session.getId());

        int newMsgCount = msgCount.incrementAndGet();
        if (newMsgCount > MAX_MSG_PER_HOUR + 1) {
            msgCount.decrementAndGet();
            return REJECT;
        } else if (newMsgCount > MAX_MSG_PER_HOUR) {
            return INFORMED_REJECT;
        }
        return ACCEPT;
    }

    @Scheduled(initialDelay = 1, fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void clearMessageCount() {
        messageCount.clear();
    }
}
