package com.example.serverdatabase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Java6Assertions.*;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ServerDatabaseApplicationTests {
    WebSocketHandler socketHandler = new WebSocketHandler();
/*
    @Test
    void getDevices() {
        System.out.println(System.getenv("MONGO_TOKEN"));
        String actual = socketHandler.getDeviceStatuses();
        System.out.println(actual);
        assertThat(actual).isNotNull();
    }*/


    @Test
    void changeTVStatus(){
        String message = "{'_id':'Bedroom TV', 'status':'true'}";
        HashMap response = socketHandler.changeDeviceStatus(message);
        assertThat(response.get("message")).isEqualTo(message);
    }

/*

    @Test
    void changeLampStatus(){
        MONGO_TOKEN = System.getenv("MONGO_TOKEN");
        System.out.println(MONGO_TOKEN);
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'true'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'false'}");
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }

    @Test
    void changeFanStatus(){
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'0'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'3'}");
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }

    @Test
    void changeThermoStatus(){
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'20.4'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'25'}");
        System.out.println(socketHandler.getDeviceStatuses());
        assertThat(twiceChange).isNotEqualTo(onceChange);
    }
*/
}
