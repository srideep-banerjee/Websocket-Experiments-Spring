package me.experiments.websockettest.handlers;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static me.experiments.websockettest.config.Constants.MESSAGE_QUEUE_LENGTH;

@Component
public class SocketConnectionHandler extends TextWebSocketHandler {

    private final Queue<WebSocketMessage<?>> messages = new ConcurrentLinkedQueue<>();
    private final AtomicInteger messageQueueSize = new AtomicInteger(0);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        for (WebSocketMessage<?> message: messages) {
            session.sendMessage(message);
        }
        sessions.put(session.getId(), session);

        System.out.println(session.getId() + " Connected");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        sessions.remove(session.getId());
        session.close();

        System.out.println(session.getId() + " Disconnected");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        messages.add(message);
        if (messageQueueSize.incrementAndGet() > MESSAGE_QUEUE_LENGTH) {
            if (messages.poll() != null) {
                messageQueueSize.decrementAndGet();
            }
        }

        for (WebSocketSession otherSession: sessions.values()) {
            if (!otherSession.getId().equals(session.getId())) {
                try {
                    otherSession.sendMessage(message);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
