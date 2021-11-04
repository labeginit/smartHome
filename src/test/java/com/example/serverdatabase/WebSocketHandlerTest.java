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
        socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"on\":\"true\"}", "testSession");
        String after = socketHandler.getTvStatus();
        System.out.println(after);
        assert(!before.equalsIgnoreCase(after));
    }

    @Test
    void changeTVStatusTst2() {
        String before = socketHandler.getTvStatus();
        socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"channel\":\"1\"}", "testSession");
        System.out.println(before);
        String after = socketHandler.getTvStatus();
        if (after.equalsIgnoreCase(before))
            socketHandler.changeDeviceStatus("{\"_id\":\"Livingroom TV\",\"channel\":\"10\"}", "testSession");
        after = socketHandler.getTvStatus();
        System.out.println(after);
        assert(!before.equalsIgnoreCase(after));
    }

}