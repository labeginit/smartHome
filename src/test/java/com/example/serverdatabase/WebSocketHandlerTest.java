package com.example.serverdatabase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketHandlerTest {

    WebSocketHandler socketHandler = new WebSocketHandler();
    @Test
    void getTvStatusTst() {
        String actual = socketHandler.getTvStatus();
        assert(!actual.isEmpty());
    }
    @Test
    void changeTVStatusTst() {
        String before = socketHandler.getTvStatus();
        socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"on\":\"true\"}");
        String after = socketHandler.getTvStatus();

        assert(before != after);
    }

}