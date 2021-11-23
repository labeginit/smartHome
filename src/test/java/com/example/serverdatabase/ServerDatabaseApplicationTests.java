package com.example.serverdatabase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;
import static org.assertj.core.api.Java6Assertions.*;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ServerDatabaseApplicationTests {
    WebSocketHandler socketHandler = new WebSocketHandler();

    @Mock
    private WebSocketSession session;

    @Test
    void getDevices() {
        String actual = socketHandler.getDeviceStatuses();
        System.out.println(actual);
        assertThat(actual).isNotNull();
    }

    @Test
    void changeTVStatus(){
        String message = "{'_id':'Bedroom TV', 'status':'true'}";
        HashMap response = socketHandler.changeDeviceStatus(message, session.getId());
        assertThat(response.get("message")).isEqualTo(message);
    }

    @Test
    void changeLampStatus(){
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'true'}", session.getId());
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'false'}", session.getId());
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }

    @Test
    void changeFanStatus(){
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'0'}", session.getId());
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'3'}", session.getId());
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }

    @Test
    void changeThermoStatus(){
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'20.4'}", session.getId());
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'25'}", session.getId());
        System.out.println(socketHandler.getDeviceStatuses());
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }

}
