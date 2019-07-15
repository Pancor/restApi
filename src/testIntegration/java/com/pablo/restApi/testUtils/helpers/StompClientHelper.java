package com.pablo.restApi.testUtils.helpers;

import com.pablo.restApi.models.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StompClientHelper {

    private WebSocketStompClient client;
    private StompSession session;

    private String URL;
    private WebSocketHttpHeaders headers;
    private CustomSessionHandler handler;
    private CompletableFuture<Object> result;

    public StompClientHelper(String URL, WebSocketHttpHeaders headers) {
        this.URL = URL;
        this.headers = headers;
        this.result = new CompletableFuture<>();
        this.handler = new CustomSessionHandler();
        createClient();
    }

    public CompletableFuture<StompSession> connect() {
            return CompletableFuture.supplyAsync(() -> {
                        try {
                            return session = client.connect(URL, headers, handler).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
    }

    public CompletableFuture<Void> subscribe(String destination) {
        return CompletableFuture.runAsync(() -> session.subscribe(destination, handler));
    }

    public CompletableFuture<Object> send(String destination, Object payload, Class responseType) {
        handler.setPayloadType(responseType);
        return CompletableFuture.supplyAsync(() -> {
            session.send(destination, payload);
            return result;
        });
    }

    private void createClient() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        client = new WebSocketStompClient(new SockJsClient(transports));
        client.setMessageConverter(new MappingJackson2MessageConverter());
    }

    private class CustomSessionHandler extends StompSessionHandlerAdapter {

        private Class payloadType;

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            result.complete(payload);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return payloadType;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            throw new RuntimeException(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            throw new RuntimeException(exception);
        }

        void setPayloadType(Class payloadType) {
            this.payloadType = payloadType;
        }
    }
}
