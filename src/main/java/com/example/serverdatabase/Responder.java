package com.example.serverdatabase;

import com.example.serverdatabase.DeviceTypes.Curtain;
import com.example.serverdatabase.DeviceTypes.Lamp;
import com.example.serverdatabase.DeviceTypes.Thermometer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class Responder implements WebMvcConfigurer {


    /*
    Curl POST Request for different devices
    lamp = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Kitchen Lamp\",\"on\":\"false\"}" -s | jq
    thermometer = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom Thermometer\",\"temp\":\"19.2\"}" -s | jq
    curtain = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"_id\":\"Livingroom Curtain\",\"open\":\"false\"}" -s | jq
     */


    @RequestMapping(value = "/changeDeviceStatus", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public Object postResponse(@RequestBody String keyword) {
        JsonObject userInput = new JsonParser().parse(keyword).getAsJsonObject(); // User POST Request
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get("_id")).replace("\"", "");
        Document dbResponse = DBConnector.findDevice(deviceID);

        String deviceToBeChanged = dbResponse.get("device").toString();

        if (deviceToBeChanged.equals("lamp")) {
            String on = String.valueOf(userInput.get("on")).replace("\"", "");
            response = lampHandler(dbResponse, deviceID, on, userInput);
        }
        if (deviceToBeChanged.equals("thermometer")) {
            String temp = String.valueOf(userInput.get("temp")).replace("\"", "");
            response = thermometerHandler(dbResponse, deviceID, temp, userInput);
        }
        if (deviceToBeChanged.equals("curtain")) {
            boolean open = Boolean.parseBoolean(userInput.get("open").toString().replace("\"", ""));
            response = curtainHandler(dbResponse, deviceID, open, userInput);
        }

        if (!(response == null)) {
            if (response.get("operation").equals("success"))
                WebSocketHandler.broadcastMessage(String.valueOf(response));
            return response;
        } else
            return "An error has occurred, please try again";
    }

    @GetMapping("/getAllDeviceStatuses")
    public String allStatuses() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        ArrayList<Object> responseMap = new ArrayList<>();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            String deviceType = (String) article.get("device");
            String id = String.valueOf(article.get("_id"));
            if (deviceType.equals("lamp")) {
                Lamp lamp = new Lamp(id, Boolean.parseBoolean(article.get("on").toString()));
                responseMap.add(lamp);
            }
            if (deviceType.equals("thermometer")) {
                Thermometer thermometer = new Thermometer(id, Double.parseDouble(article.get("temp").toString()));
                responseMap.add(thermometer);
            }
            if (deviceType.equals("curtain")) {
                Curtain curtain = new Curtain(id, Boolean.parseBoolean(article.get("open").toString()));
                responseMap.add(curtain);
            }
        }
        Gson gson = new Gson();
        return gson.toJson(responseMap);
    }

    private static HashMap<String, String> lampHandler(Document dbResponse, String deviceID, String on, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("on").toString().equals(on))) {
                DBConnector.changeDeviceStatus("lamp", jsonObject);
                response.put("device", "lamp");
                response.put("option", on);
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + on);
            }
            return response;
        }
        return null;
    }

    private static HashMap<String, String> thermometerHandler(Document dbResponse, String deviceID, String temp, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("temp").toString().equals(temp))) {
                DBConnector.changeDeviceStatus("thermometer", jsonObject);
                response.put("device", "thermometer");
                response.put("option", temp);
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + temp);
            }
            return response;
        }
        return null;
    }

    private static HashMap<String, String> curtainHandler(Document dbResponse, String deviceID, boolean open, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("open").toString().equals(String.valueOf(open)))) {
                DBConnector.changeDeviceStatus("curtain", jsonObject);
                response.put("device", "curtain");
                response.put("option", String.valueOf(open));
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + open);
            }
            return response;
        }
        return null;
    }
}