package com.example.websocket_demo.client;

import com.example.websocket_demo.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyStompClient {

    private final String username;
    private final StompSession session;

    public MyStompClient(MessageListener messageListener, String username) throws ExecutionException, InterruptedException {
        this.username = username;
        this.session = createStompClient(messageListener, username);
    }

    private StompSession createStompClient(MessageListener messageListener, String username) throws ExecutionException, InterruptedException {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        List<Transport> transports = List.of(new WebSocketTransport(webSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new MyStompSessionHandler(messageListener, username);
        String url = "ws://localhost:8080/ws";

        System.out.println("Connecting to WebSocket at " + url);
        return stompClient.connectAsync(url, sessionHandler).get();
    }

    public void sendMessage(Message message) {
        if (session == null || !session.isConnected()) {
            System.out.println("Cannot send message, STOMP session is not connected.");
            return;
        }
        try {
            session.send("/app/message", message);
            System.out.println("Message sent: " + message.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectUser(String username) {
        if (session == null || !session.isConnected()) {
            System.out.println("Cannot disconnect user, STOMP session is not connected.");
            return;
        }
        try {
            session.send("/app/disconnect", username);
            System.out.println("User disconnected: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
