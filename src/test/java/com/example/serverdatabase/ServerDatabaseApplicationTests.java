package com.example.serverdatabase;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class ServerDatabaseApplicationTests {
    @Mock
    WebSocketSession session;
    WebSocketHandler socketHandler = new WebSocketHandler();

    @Test
    @Order(1)
    void changeStatusTV() {
        String message = "{'_id':'Bedroom TV', 'status':'true'}";
        HashMap response = socketHandler.changeDeviceStatus(message);
        assert(response.get("message")).equals(message);
    }
    /*
    @Test
    @Order(2)
    void getDevices() {
        String actual = socketHandler.getDeviceStatuses();
        assert(actual != null);
    }

    @Test
    @Order(3)
    void addThermo() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Thermometer',device:'thermometer','status':'20'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(3)
    void addLamp() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Lamp',device:'lamp','status':'false'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(3)
    void addLampTwice() {
        socketHandler.addNewDevice("{'_id':'Test Lamp',device:'lamp','status':'false'}", session.getId());
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Lamp',device:'lamp','status':'false'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(twiceChange.equals(onceChange));
    }

    @Test
    @Order(3)
    void addAlarm() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Alarm',device:'alarm','status':'0'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(3)
    void addHeater() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Heater',device:'heater','status':'false'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(3)
    void addFan() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.addNewDevice("{'_id':'Test Fan',device:'fan','status':'0'}", session.getId());
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(4)
    void removeThermo() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.removeDevice("{'_id':'Test Thermometer'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(4)
    void removeLamp() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.removeDevice("{'_id':'Test Lamp'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(4)
    void removeAlarm() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.removeDevice("{'_id':'Test Alarm'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(4)
    void removeHeater() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.removeDevice("{'_id':'Test Heater'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(4)
    void removeFan() {
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.removeDevice("{'_id':'Test Fan'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(5)
    void changeStatusLamp() {
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'true'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Outdoor lamp', 'status':'false'}");
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(5)
    void changeStatusFan() {
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'4'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Bedroom Fan', 'status':'0'}");
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(5)
    void changeStatusThermo() {
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'20.4'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'Livingroom Thermometer', 'status':'25'}");
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(5)
    void changeStatusAlarm() {
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'alarm', 'status':'1'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'alarm', 'status':'0'}");
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(5)
    void changeStatusHeater() {
        HashMap onceChange = socketHandler.changeDeviceStatus("{'_id':'House Heater', 'status':'true'}");
        HashMap twiceChange = socketHandler.changeDeviceStatus("{'_id':'House Heater', 'status':'false'}");
        assert(!twiceChange.equals(onceChange));
    }


    @Test
    @Order(6)
    void getConfirmationLamp() {
        socketHandler.getConfirmation("{'_id':'Indoor lamp',device:'lamp','status':'true', result:'success'}");
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.getConfirmation("{'_id':'Indoor lamp',device:'lamp','status':'false', result:'success'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(6)
    void getConfirmationAlarm() {
        socketHandler.getConfirmation("{'_id':'alarm',device:'alarm','status':'1', result:'success'}");
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.getConfirmation("{'_id':'alarm',device:'alarm','status':'0', result:'success'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(6)
    void getConfirmationFan() {
        socketHandler.getConfirmation("{'_id':'Bedroom Fan',device:'fan','status':'3', result:'success'}");
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.getConfirmation("{'_id':'Bedroom Fan',device:'fan','status':'0', result:'success'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(6)
    void getConfirmationHeater() {
        socketHandler.getConfirmation("{'_id':'House Heater',device:'heater','status':'true', result:'success'}");
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.getConfirmation("{'_id':'House Heater',device:'heater','status':'false', result:'success'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }

    @Test
    @Order(6)
    void getConfirmationThermo() {
        socketHandler.getConfirmation("{'_id':'Livingroom Thermometer',device:'thermometer','status':'20', result:'success'}");
        String onceChange = socketHandler.getDeviceStatuses();
        socketHandler.getConfirmation("{'_id':'Livingroom Thermometer',device:'thermometer','status':'18', result:'success'}");
        String twiceChange = socketHandler.getDeviceStatuses();
        assert(!twiceChange.equals(onceChange));
    }
*/
}
