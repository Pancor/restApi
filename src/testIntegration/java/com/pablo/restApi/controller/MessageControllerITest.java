package com.pablo.restApi.controller;

import com.pablo.restApi.models.Message;
import com.pablo.restApi.testUtils.helpers.StompClientHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MessageControllerITest {

    @Value("${local.server.port}")
    private String port;

    private StompClientHelper stompClientHelper;
    private CompletableFuture<StompSession> cf;

    @Before
    public void setUp() {
        String URL = "ws://localhost:" + port + "/messenger";
        WebSocketHttpHeaders header = getAuthorizationHeaderForUser("boob", "b00b");
        stompClientHelper = new StompClientHelper(URL, header);
        cf = CompletableFuture.supplyAsync(stompClientHelper.connect());
    }

    @Test
    public void sendMessageByWebSocketThenReceiveIt() {
        Message response = (Message) cf.thenApply(stompClientHelper.subscribe("/topic/answer"))
                .thenCompose(stompClientHelper.send("/app/send", new Message("mess"), Message.class))
                .join();

        assertEquals("Received message should contain message, which was send", new Message("You just said: mess"), response);
    }

    @Test
    public void getServerStatusByWebSocketThenReceiveServerStatus() {
        Message response = (Message) cf.thenApply(stompClientHelper.subscribe("/topic/status"))
                .thenCompose(stompClientHelper.send("/app/status", new Message(""), Message.class))
                .join();

        assertEquals("Server status should always by OK", new Message("OK"), response);
    }

    private WebSocketHttpHeaders getAuthorizationHeaderForUser(String username, String plainPassword) {
        String plainCredentials=username + ":" + plainPassword;
        String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes());
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        return headers;
    }
}


