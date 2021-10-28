package com.example.serverdatabase;

import com.example.serverdatabase.DeviceConnector.HttpHandler;
import com.example.serverdatabase.DeviceTypes.Curtain;
import com.example.serverdatabase.DeviceTypes.Lamp;
import com.example.serverdatabase.DeviceTypes.Thermometer;
import com.example.serverdatabase.Singleton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
        if (currentState.equalsIgnoreCase("false")){
            newState = "true";
        } else {newState = "false";}
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

    //This End point for testing
    @RequestMapping(value = "/changeStatus", method = RequestMethod.PUT, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public String changeStatus() throws IOException, InterruptedException, JSONException {
        // We can change them from the input from the Unit group.
        String deviceId = "1";
        String status = "on";
        httpHandler = new HttpHandler();
        httpHandler.changeLampStatus(deviceId, status);
        String re = httpHandler.changeLampStatus(deviceId, status);
        return re;
    }

    public void changeLampStatus(String deviceId, String status) throws JSONException, IOException, InterruptedException {
        httpHandler = new HttpHandler();
        String response = httpHandler.changeLampStatus(deviceId, status);
        if (response.equalsIgnoreCase("ok")) {
            //Update the status in the database
        } else {
            //send an error message to the Unit
        }

    }
}