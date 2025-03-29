package me.experiments.websockettest.handlers;

import me.experiments.websockettest.rate.limit.RateLimitingInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class SocketConnectionHandler extends TextWebSocketHandler {

    static int MESSAGE_QUEUE_LENGTH = 10;

    final Queue<WebSocketMessage<?>> messages = new LinkedList<>();
    final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        synchronized (sessions) {
            for (WebSocketMessage<?> message: messages) {
                session.sendMessage(message);
            }
            sessions.put(session.getId(), session);
        }

        System.out.println(session.getId() + " Connected");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        synchronized (sessions) {
            sessions.remove(session.getId());
        }

        System.out.println(session.getId() + " Disconnected");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        synchronized (sessions) {

            messages.add(message);
            if (messages.size() > MESSAGE_QUEUE_LENGTH) {
                messages.poll();
            }

            for (WebSocketSession otherSession: sessions.values()) {
                if (!otherSession.getId().equals(session.getId())) {
                    otherSession.sendMessage(message);
                }
            }
        }
    }
}
