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
    void contextLoads() {
        //   String actual = socketHandler.getDeviceStatuses();
        //   assertThat(actual).isNotNull();
    }

    @Test
    void changeTVStatus(){
        String message = "{'_id':'Bedroom TV', 'status':'true'}";
        HashMap response = socketHandler.changeDeviceStatus(message, session.getId());
        assertThat(response.get("message")).isEqualTo(message);
    }

}
