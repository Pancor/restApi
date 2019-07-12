package com.pablo.restApi.controller;

import com.pablo.restApi.models.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MessageControllerITest {

    @Value("${local.server.port}")
    private String port;

    private StompSession session;
    private BlockingQueue<Message> receivedMessages;
    private CompletableFuture cf;

    @Before
    public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        String URL = "ws://127.0.0.1:" + port + "/messenger";
        WebSocketHttpHeaders header = getAuthorizationHeaderForUser("boob", "b00b");

        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient client = new WebSocketStompClient(new SockJsClient(transports));
        client.setMessageConverter(new MappingJackson2MessageConverter());

        cf = new CompletableFuture();
        receivedMessages = new LinkedBlockingDeque<>();
        session = client.connect(URL, header, new CustomSessionHandler()).get(3, TimeUnit.SECONDS);
        cf.join();
    }

    @Test
    public void sendMessageByWebSocketThenReceiveIt() throws InterruptedException {
        session.send("/app/send", new Message("mess"));
        Message response = receivedMessages.poll(3, TimeUnit.SECONDS);

        assertEquals("Received message should contain message, which was send", new Message("You just said: mess"), response);
    }

    @Test
    public void getServerStatusByWebSocketThenReceiveServerStatus() throws InterruptedException {
        session.send("/app/status", "");
        Message response = receivedMessages.poll(3, TimeUnit.SECONDS);

        assertEquals("Server status should always by OK", new Message("OK"), response);
    }

    private WebSocketHttpHeaders getAuthorizationHeaderForUser(String username, String plainPassword) {
        String plainCredentials=username + ":" + plainPassword;
        String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes());
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        return headers;
    }

    private class CustomSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe("/topic/answer", this);
            session.subscribe("/topic/status", this);
            cf.complete(null);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessages.offer((Message) payload);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            throw new RuntimeException(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            throw new RuntimeException(exception);
        }
    }
}


