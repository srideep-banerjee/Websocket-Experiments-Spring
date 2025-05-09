package me.experiments.websockettest.sockets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static me.experiments.websockettest.Constants.MESSAGE_QUEUE_LENGTH;

@Component
public class SocketConnectionHandler extends TextWebSocketHandler {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    private final Queue<WebSocketMessage<?>> messages = new ConcurrentLinkedQueue<>();
    private final AtomicInteger messageQueueSize = new AtomicInteger(0);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public SocketConnectionHandler(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        for (WebSocketMessage<?> message: messages) {
            session.sendMessage(message);
        }
        session = new ConcurrentWebSocketSessionDecorator(session, 10000, 10000);
        sessions.put(session.getId(), session);

        System.out.println(session.getId() + " Connected");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        sessions.remove(session.getId());
        session.close();

        rateLimitingInterceptor.afterConnectionClosed(session);

        System.out.println(session.getId() + " Disconnected");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        byte response = rateLimitingInterceptor.registerNewMessage(session);
        if (response == RateLimitingInterceptor.INFORMED_REJECT) {
            session.sendMessage(new TextMessage("Exhausted per hour message limit!"));
        }
        if (response != RateLimitingInterceptor.ACCEPT) {
            return;
        }

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
