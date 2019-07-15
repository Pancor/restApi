package com.pablo.restApi.controller;

import com.pablo.restApi.models.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @MessageMapping("/send")
    @SendTo("/topic/answer")
    public Message sendMessage(Message msg) {
        return new Message("You just said: " + msg.getMsg());
    }

    @MessageMapping("/status")
    @SendTo("/topic/status")
    public Message getStatus() {
        return new Message("OK");
    }

    @MessageExceptionHandler
    @SendToUser("/topic/error")
    public Message handleExceptions(Exception ex) {
        System.err.println("Error: " + ex.getMessage());
        return new Message("Some strange error occurred");
    }
}
