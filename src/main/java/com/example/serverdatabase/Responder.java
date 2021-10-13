package com.example.serverdatabase;

import com.example.serverdatabase.DeviceTypes.Lamp;
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
    Curl POST Request = curl -X POST http://localhost:8080/changeDeviceStatus -H "Content-Type: application/json" -d "{\"deviceName\":\"Kitchen Lamp\",\"status\":\"off\"}" -s | jq
     */


    @RequestMapping(value = "/changeDeviceStatus", method = RequestMethod.POST, headers = "Accept=*/*", produces = "application/json", consumes = "application/json")
    public Object postResponse(@RequestBody String keyword) {
        JsonObject jsonObject = new JsonParser().parse(keyword).getAsJsonObject();

        String deviceName = String.valueOf(jsonObject.get("deviceName")).replace("\"", "");
        String status = String.valueOf(jsonObject.get("status")).replace("\"", "");

        Document dbResponse = DBConnector.findDevice(deviceName);
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("status").equals(status))) {
                DBConnector.changeDeviceStatus(deviceName, status);
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceName + " is already " + status);
            }
            return response;
        }
        return "An error has occurred, please try again";
    }

    @GetMapping("/getAllDeviceStatuses")
    public String allStatuses() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        ArrayList<Object> responseMap = new ArrayList<>();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            String deviceType = (String) article.get("device");
            if (deviceType.equals("Lamp")) {
                Lamp lamp = new Lamp(String.valueOf(article.get("_id")), String.valueOf(article.get("status")), String.valueOf(article.get("name")));
                responseMap.add(lamp);
            }
        }
        Gson gson = new Gson();
        return gson.toJson(responseMap);
    }

    @GetMapping("/testAPI")
    public String welcomePage() {
        return "API is working!";
    }
}


