package me.experiments.websockettest.rooms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

public class RoomInterceptor  implements HandshakeInterceptor {

    private final String path;

    public RoomInterceptor(@Value("${websocket.path}") String path) {
        this.path = path;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        UriTemplate uriTemplate = new UriTemplate(path);
        Map<String, String> pathParams = uriTemplate.match(request.getURI().getPath());
        attributes.putAll(pathParams);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}
