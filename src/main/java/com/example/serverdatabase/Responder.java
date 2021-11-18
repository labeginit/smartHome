package com.example.serverdatabase;

import com.example.serverdatabase.DeviceConnector.HttpHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.IOException;

@RestController
public class Responder implements WebMvcConfigurer {
    private final static String TV = "TV";

    private HttpHandler httpHandler;

    /*
    Curl POST Request for different devices
    lamp = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Kitchen Lamp\",\"on\":\"false\"}" -s | jq
    thermometer = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom Thermometer\",\"temp\":\"19.2\"}" -s | jq
     */

    public void changeDeviceStatus(String deviceId, String status, String deviceType) throws JSONException, IOException, InterruptedException {
        httpHandler = new HttpHandler();
        if (deviceType.equalsIgnoreCase("lamp")) {
            httpHandler.changeLampStatus(deviceId, status, deviceType);
        } else if (deviceType.equalsIgnoreCase("fan")) {
            //httpHandler.changeFanStatus()
        } else if (deviceType.equalsIgnoreCase("thermometer")) {
            //httpHandler.changeThermometerStatus()
        } else if (deviceType.equalsIgnoreCase("curtain")) {
            //httpHandler.changeCurtainStatus()
        } else {
            //Error massage
        }
    }

    //This End point for testing
    @RequestMapping(value = "/changeStatus/", method = RequestMethod.PUT, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public String changeStatus(String deviceId, String status, String deviceType) throws IOException, InterruptedException, JSONException {
        // We can change them from the input from the Unit group.
        deviceId = "1";
        status = "on";
        httpHandler = new HttpHandler();
        httpHandler.changeLampStatus(deviceId, status, deviceType);
        String re = httpHandler.changeLampStatus(deviceId, status, deviceType);
        return re;
    }

    //For testing
    @RequestMapping(value = "/changeStatusToOff/", method = RequestMethod.PUT, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public String changeStatusToOff(String deviceId, String status, String deviceType) throws IOException, InterruptedException, JSONException {
        // We can change them from the input from the Unit group.
        deviceId = "1";
        status = "off";
        httpHandler = new HttpHandler();
        httpHandler.changeLampStatus(deviceId, status, deviceType);
        String re = httpHandler.changeLampStatus(deviceId, status, deviceType);
        return re;
    }

    // For testing for now change device to On is working but not off after On, device group with check it.

    @PostMapping(value = "/sendConfirmation1", headers = "Accept=", produces = "application/json", consumes = "application/json")
    public Object postResponse(@RequestBody String keyword) {
        JsonObject jsonObject = new JsonParser().parse(keyword).getAsJsonObject();
        String deviceId = String.valueOf(jsonObject.get("deviceId")).replace("\"", "");
        String status = String.valueOf(jsonObject.get("status")).replace("\"", "");

        if (deviceId.equals("1") && status.equals("on")) {
            System.out.println("lamp is on");
            //We can send it to the DB updater
            return "Lamp On";
        } else if (deviceId.equals("1") && status.equals("off")) {
            System.out.println("lamp is off");
            //We can send it to the DB updater
            return "Lamp Off";
        }
        return "Something went wrong";
    }

    @PostMapping(value = "/sendConfirmation2", headers = "Accept=", produces = "application/json", consumes = "application/json")
    public Object confirmationFromDevice(@RequestBody String keyword) {
        JsonObject jsonObject = new JsonParser().parse(keyword).getAsJsonObject();
        String deviceType = String.valueOf(jsonObject.get("deviceType")).replace("\"", "");
        String deviceId = String.valueOf(jsonObject.get("_id")).replace("\"", "");
        String status = String.valueOf(jsonObject.get("on")).replace("\"", "");

        if (deviceType.equalsIgnoreCase("lamp")) {
            if (deviceId.equals("Outdoor lamp") && status.equals("true")) {
                System.out.println("lamp is on");
                DBConnector.changeDeviceStatus("lamp", jsonObject);
                return "Lamp On";
            } else if (deviceId.equals("Outdoor lamp") && status.equals("false")) {
                System.out.println("lamp is off");
                DBConnector.changeDeviceStatus("lamp", jsonObject);
                return "Lamp Off";
            }
        } else if (deviceType.equalsIgnoreCase("fan")) {
            if (deviceId.equals("") && status.equals("")) {

            } else if (deviceId.equals("") && status.equals("")) {

            }
        }
        return "Something went wrong";
    }

}