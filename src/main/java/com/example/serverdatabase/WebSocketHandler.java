package com.example.serverdatabase;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final ArrayList<WebSocketSession> clients = new ArrayList<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if (!(checkIfExists(session.getId()))) {
            clients.add(session);
            System.out.println(session.getId() + " Just connected!");
        }
        String msg = message.getPayload();
        Responder responder = new Responder();
        switch (msg) {
            case ("getDevices"):
                session.sendMessage(new TextMessage(responder.allStatuses()));
                break;
            case ("2"):
                System.out.println("Cat button was pressed");
                session.sendMessage(new TextMessage("Meooow"));
                break;
            case ("3"):
                System.out.println("Pig button was pressed");
                session.sendMessage(new TextMessage("Bork Bork"));
                break;
            case ("4"):
                System.out.println("Fox button was pressed");
                session.sendMessage(new TextMessage("Fraka-kaka-kaka"));
                break;
            default:
                System.out.println("Connected to Client");
        }
    }

    public static void broadcastMessage(String test) {
        for (WebSocketSession cl : clients) {
            try {
                if (cl.isOpen())
                    cl.sendMessage(new TextMessage(test));
            } catch (Exception e) {
                clients.remove(cl);
            }
        }
    }

    private static boolean checkIfExists(String clientID) {
        for (WebSocketSession client : clients) {
            if (client.getId().equals(clientID)) {
                return true;
            }
        }
        return false;
    }
}