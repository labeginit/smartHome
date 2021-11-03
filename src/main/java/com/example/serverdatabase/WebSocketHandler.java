package com.example.serverdatabase;

import com.example.serverdatabase.DeviceConnector.HttpHandler;
import com.example.serverdatabase.DeviceTypes.Curtain;
import com.example.serverdatabase.DeviceTypes.Lamp;
import com.example.serverdatabase.DeviceTypes.Thermometer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final ArrayList<WebSocketSession> clients = new ArrayList<>();
    private final static String TV = "TV";

    private HttpHandler httpHandler;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        if (!(checkIfExists(session.getId()))) {
            clients.add(session);
            System.out.println(session.getId() + " Just connected!");
        }
        String operation = message.getPayload().split("=", message.getPayload().length())[0];
        String jsonData = "";
        try {
            jsonData = message.getPayload().split("=", message.getPayload().length())[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
        switch (operation) {
            case ("getDevices"):
                session.sendMessage(new TextMessage(getDeviceStatuses()));
                break;
            case ("changeDeviceStatus"):
               // changeDeviceStatus(jsonData);
                session.sendMessage(new TextMessage(String.valueOf(changeDeviceStatus(jsonData))));
                break;
            case ("getTVStatus"):
                session.sendMessage(new TextMessage(getTvStatus()));
                break;
            default:
                System.out.println("Connected to Client");
        }
    }

    public static void broadcastMessage(String test) {
        for (WebSocketSession cl : clients) {
            try {
                if (cl.isOpen())
                    cl.sendMessage(new TextMessage(test));
            } catch (Exception e) {
                clients.remove(cl);
            }
        }
    }

    private static boolean checkIfExists(String clientID) {
        for (WebSocketSession client : clients) {
            if (client.getId().equals(clientID)) {
                return true;
            }
        }
        return false;
    }

    public Object changeDeviceStatus(String message) {
        System.out.println(message);
        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject(); // User POST Request
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get("_id")).replace("\"", "");
        Document dbResponse = DBConnector.findDevice(deviceID);
        String deviceToBeChanged = "";
        if (dbResponse != null) {
            deviceToBeChanged = dbResponse.get("device").toString();

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
            if (deviceToBeChanged.equals(TV)) {
                if (userInput.has("on")) {
                    response = tvHandlerState(dbResponse, userInput);
                } else if (userInput.has("channel")){
                    String channel = String.valueOf(userInput.get("channel")).replace("\"", "");
                    response = tvHandlerChannel(dbResponse, deviceID, channel, userInput);
                }
                WebSocketHandler.broadcastMessage(message);
            }
        }
        if (!(response == null)) {
            if (!deviceToBeChanged.equalsIgnoreCase(TV) && response.get("operation").equals("success"))
                WebSocketHandler.broadcastMessage(String.valueOf(response));
            return response;
        } else
            return "An error has occurred, please try again";
    }

    private static String getDeviceStatuses() {
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

    private static HashMap<String, String> tvHandlerState(Document dbResponse, JsonObject jsonObject) {
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

    private static HashMap<String, String> tvHandlerChannel(Document dbResponse, String deviceID, String channel, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        if (!(dbResponse.get("channel").toString().equals(channel))) {
            DBConnector.changeDeviceStatus(TV, jsonObject);
            response.put("device", TV);
            response.put("option", channel);
            response.put("operation", "success");
        } else {
            response.put("operation", "failed");
            response.put("reason", deviceID + " is already " + channel);
        }
        return response;
    }
}
