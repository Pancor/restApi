package com.pablo.restApi.controller;

import com.pablo.restApi.models.Message;
import com.pablo.restApi.testUtils.helpers.StompClientHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MessageControllerITest {

    @LocalServerPort
    private String port;

    @Autowired
    private StompClientHelper stompClientHelper;

    private CompletableFuture<StompSession> stompSession;

    @Before
    public void setUp() {
        String URL = "ws://localhost:" + port + "/messenger";
        WebSocketHttpHeaders header = getAuthorizationHeaderForUser("boob", "b00b");
        stompSession = CompletableFuture.supplyAsync(stompClientHelper.connect(URL, header));
    }

    @Test
    public void sendMessageByWebSocketThenReceiveIt() {
        Message response = (Message) stompSession.thenApply(stompClientHelper.subscribeTo("/topic/answer"))
                .thenCompose(stompClientHelper.send("/app/send", new Message("mess"), Message.class))
                .orTimeout(3, TimeUnit.SECONDS)
                .join();

        assertEquals("Received message should contain message, which was send",
                new Message("You just said: mess"), response);
    }

    @Test
    public void sendEmptyMessageByWebSocketThenReceiveResponse() {
        Message response = (Message) stompSession.thenApply(stompClientHelper.subscribeTo("/topic/answer"))
                .thenCompose(stompClientHelper.send("/app/send", new Message(""), Message.class))
                .orTimeout(3, TimeUnit.SECONDS)
                .join();

        assertEquals("Received message should contain only server message without user message",
                new Message("You just said: "), response);
    }

    @Test
    public void getServerStatusByWebSocketThenReceiveServerStatus() {
        Message response = (Message) stompSession.thenApply(stompClientHelper.subscribeTo("/topic/status"))
                .thenCompose(stompClientHelper.send("/app/status", new Message(""), Message.class))
                .orTimeout(3, TimeUnit.SECONDS)
                .join();

        assertEquals("Server status should always be OK", new Message("OK"), response);
    }

    @After
    public void cleanUp() {
        stompClientHelper.cleanUp().join();
    }

    private WebSocketHttpHeaders getAuthorizationHeaderForUser(String username, String plainPassword) {
        String plainCredentials = username + ":" + plainPassword;
        String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes());
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        return headers;
    }
}


