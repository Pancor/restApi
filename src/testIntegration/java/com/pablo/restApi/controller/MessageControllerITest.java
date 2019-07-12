package com.pablo.restApi.controller;

import com.pablo.restApi.models.Message;
import com.pablo.restApi.testUtils.users.BobUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
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
@BobUser
@ActiveProfiles("test")
public class MessageControllerITest {

    private static final String SUBSCRIPTION_TOPIC = "/topic/answer";

    @Value("${local.server.port}")
    private String port;

    private StompSession session;
    private BlockingQueue<Message> receivedMessages;

    @Before
    public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        String URL = "ws://127.0.0.1:" + port + "/messenger";
        WebSocketHttpHeaders header = getAuthorizationHeader("boob", "b00b");

        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketStompClient client = new WebSocketStompClient(new SockJsClient(transports));
        client.setMessageConverter(new MappingJackson2MessageConverter());

        receivedMessages = new LinkedBlockingDeque<>();
        session = client.connect(URL, header, new CustomSessionHandler()).get(5, TimeUnit.SECONDS);
    }

    @Test
    public void sendMessageByWebSocketThenReceiveIt() throws InterruptedException {
        session.send("/app/send", new Message("mess"));
        Message response = receivedMessages.poll(5, TimeUnit.SECONDS);

        assertEquals(new Message("You just said: mess"), response);
    }

    @After
    public void tearDown() {
        session.disconnect();
    }

    private WebSocketHttpHeaders getAuthorizationHeader(String username, String plainPassword) {
        String plainCredentials=username + ":" + plainPassword;
        String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes());
        final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        return headers;
    }

    private class CustomSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
            System.out.println(exception);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            session.subscribe(SUBSCRIPTION_TOPIC, this);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessages.offer((Message) payload);
        }
    }

//    @TestConfiguration
//    static class CustomTaskExecutor {
//
//        @Bean
//        TaskExecutor syncTaskExecutor() {
//            return new SyncTaskExecutor();
//        }
//    }
}


