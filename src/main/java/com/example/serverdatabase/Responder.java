package com.example.serverdatabase;

import com.example.serverdatabase.DeviceConnector.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class Responder implements WebMvcConfigurer {
    private final static String TV = "TV";

    private HttpHandler httpHandler;

    /*
    Curl POST Request for different devices
    lamp = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Kitchen Lamp\",\"on\":\"false\"}" -s | jq
    thermometer = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom Thermometer\",\"temp\":\"19.2\"}" -s | jq
    curtain = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom Curtain\",\"open\":\"false\"}" -s | jq
    tv = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom TV\",\"on\":\"true\"}" -s | jq
     */

    private static HashMap<String, String> tvHandler(Document dbResponse, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        String currentState = dbResponse.get("on").toString();
        String newState;
        if (currentState.equalsIgnoreCase("false")) {
            newState = "true";
        } else {
            newState = "false";
        }
        jsonObject.remove("on");
        jsonObject.addProperty("on", newState);  //we modify jsonObject in order to update the state of the device.
        DBConnector.changeDeviceStatus(TV, jsonObject);
        response.put("device", TV);
        response.put("option", newState);
        response.put("operation", "success");

        return response;
    }
/*
    @GetMapping("/getTVStatus")
    public String getTvStatus() {
        ArrayList<Object> responseMap = new ArrayList<>();
        boolean tvState = Singleton.getInstance().getState();
        int channel = Singleton.getInstance().getChannel();
        responseMap.add(tvState);
        responseMap.add(channel);
        Gson gson = new Gson();
        return gson.toJson(responseMap);
    }*/

    @GetMapping("/getTVStatus")
    public String getTvStatus() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        ArrayList<Object> responseMap = new ArrayList<>();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            if (article.get("device").equals(TV)) {
                String id = article.getString("_id");
                boolean state = Boolean.parseBoolean(article.get("on").toString());
                int channel = Integer.parseInt(article.get("channel").toString());
                responseMap.add(id);
                responseMap.add(state);
                responseMap.add(channel);
            }
        }
        Gson gson = new Gson();
        return gson.toJson(responseMap);
    }
/*
    private static HashMap<String, String> tvHandler(String deviceID, boolean state, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();  //no DB involved in this method.

        //we are not going to use the value received from the Mindwave, we will invert the current state
        boolean currentState = Singleton.getInstance().getState();
        boolean newState = !currentState;
        Singleton.getInstance().setState(newState);
        response.put("device", TV);
        response.put("option", String.valueOf(newState));
        response.put("operation", "success");
        return response;
    }*/


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
        String deviceName = String.valueOf(jsonObject.get("deviceName")).replace("\"", "");
        String deviceId = String.valueOf(jsonObject.get("deviceId")).replace("\"", "");
        String status = String.valueOf(jsonObject.get("status")).replace("\"", "");

        if (deviceName.equalsIgnoreCase("lamp")) {
            if (deviceId.equals("1") && status.equals("on")) {
                System.out.println("lamp is on");
                //We can send it to the DB updater
                return "Lamp On";
            } else if (deviceId.equals("1") && status.equals("off")) {
                System.out.println("lamp is off");
                //We can send it to the DB updater
                return "Lamp Off";
            }
        } else if (deviceName.equalsIgnoreCase("fan")) {
            if (deviceId.equals("") && status.equals("")) {

            } else if (deviceId.equals("") && status.equals("")) {

            }
        }
        return "Something went wrong";
    }

}