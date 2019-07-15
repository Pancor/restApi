package com.pablo.restApi.testUtils.helpers;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class StompClientHelper {

    private WebSocketStompClient client;
    private StompSession session;
    private CustomSessionHandler handler;

    public StompClientHelper() {
        this.handler = new CustomSessionHandler();
        createStompClient();
    }

    public Supplier<StompSession> connect(String URL, WebSocketHttpHeaders headers) {
        return () -> {
            try {
                return session = client.connect(URL, headers, handler).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Function<StompSession, StompSession> subscribeTo(String destination) {
        return stompSession -> {
            stompSession.subscribe(destination, handler);
            return stompSession;
        };
    }

    public Function<StompSession, CompletionStage<Object>> send(String destination, Object payload, Class responseType) {
        return stompSession -> {
            handler.setPayloadType(responseType);
            stompSession.send(destination, payload);
            return handler.getResult();
        };
    }

    public CompletableFuture<Void> cleanUp() {
        return CompletableFuture.runAsync(() -> {
            session.disconnect();
            handler.clear();
        });
    }

    private void createStompClient() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        client = new WebSocketStompClient(new SockJsClient(transports));
        client.setMessageConverter(new MappingJackson2MessageConverter());
    }

    private class CustomSessionHandler extends StompSessionHandlerAdapter {

        private CompletableFuture<Object> result;
        private Class payloadType;

        CustomSessionHandler() {
            result = new CompletableFuture<>();
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

        CompletableFuture<Object> getResult() {
            return result;
        }

        void clear() {
            result = new CompletableFuture<>();
        }
    }
}
