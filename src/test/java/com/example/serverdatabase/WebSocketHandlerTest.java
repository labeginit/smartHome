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
    void changeTVStatusTst1() {
        String before = socketHandler.getTvStatus();
        socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"on\":\"true\"}");
        String after = socketHandler.getTvStatus();

        assert(!before.equalsIgnoreCase(after));
    }

    @Test
    void changeTVStatusTst2() {
        String before = socketHandler.getTvStatus();
        socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"channel\":\"1\"}");
        String after = socketHandler.getTvStatus();
        if (after.equalsIgnoreCase(before))
            socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"channel\":\"10\"}");
        after = socketHandler.getTvStatus();
        assert(!before.equalsIgnoreCase(after));
    }

}