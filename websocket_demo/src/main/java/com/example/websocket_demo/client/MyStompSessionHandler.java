package com.example.websocket_demo.client;

import com.example.websocket_demo.Message;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {
    private String username;
    private MessageListener messageListener;

    public MyStompSessionHandler(MessageListener messageListener, String username){
        this.username = username;
        this.messageListener = messageListener;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Client Connected");

        subscribeToMessages(session);
        subscribeToUsers(session);

        session.send("/app/connect", username);
        session.send("/app/request-users", "");
    }

    private void subscribeToMessages(StompSession session) {
        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Message.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof Message message) {
                        messageListener.onMessageRecieve(message);
                        System.out.println("Received message: "+message.getUser()+": "+ message.getMessage());
                    } else {
                        System.out.println("Unexpected payload type: "+ payload.getClass());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("Subscribed to /topic/messages");
    }

    private void subscribeToUsers(StompSession session) {
        session.subscribe("/topic/users", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new ParameterizedTypeReference<List<String>>() {}.getType();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        ArrayList<String> activeUsers = (ArrayList<String>) list;
                        messageListener.onActiveUsersUpdated(activeUsers);
                        System.out.println("Received active users: " + activeUsers);
                    } else {
                        System.out.println("Unexpected users payload type: " + payload.getClass());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("Subscribed to /topic/users");
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }
}
