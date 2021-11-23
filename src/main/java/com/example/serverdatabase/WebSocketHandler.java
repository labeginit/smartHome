package com.example.serverdatabase;

import com.example.serverdatabase.DeviceConnector.CommunicatWithDevices;
import com.example.serverdatabase.DeviceConnector.HttpHandler;
import com.example.serverdatabase.DeviceTypes.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.JSONException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final ArrayList<WebSocketSession> clients = new ArrayList<>();
    private final static String TV = "TV";
    private CommunicatWithDevices communicatWithDevices;
    private HttpHandler httpHandler;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, JSONException {
        if (!(checkIfExists(session.getId()))) {
            clients.add(session);
            System.out.println(session.getId() + " " + getTime() + " Just connected!");
        }
        String operation = message.getPayload().split("=", message.getPayload().length())[0];
        String jsonData = "";
        try {
            jsonData = message.getPayload().split("=", message.getPayload().length())[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        System.out.println("here is the operation  " + operation);
        System.out.println("json data " + jsonData);

        switch (operation) {
            case ("getDevices"):
                session.sendMessage(new TextMessage(getDeviceStatuses()));
                break;
            case ("changeDeviceStatus"):
                changeDeviceStatus(jsonData, session.getId());
                break;
            case ("establishConnection"):
                break;

            default:
                System.out.println("Connected to Client");
        }
    }

    private void broadcastMessage(String test) {
        for (WebSocketSession cl : clients) {
            try {
                if (cl.isOpen())
                    cl.sendMessage(new TextMessage(test));
            } catch (Exception e) {
                clients.remove(cl);
            }
        }
    }

    private boolean checkIfExists(String clientID) {
        for (WebSocketSession client : clients) {
            if (client.getId().equals(clientID)) {
                return true;
            }
        }
        return false;
    }


    //I will keep this method if I will need it then
    public String sendMessageToDevice(String messageFromUnit, WebSocketSession socketSession) throws JSONException, IOException {
        SmartHouse smartHouse = SmartHouse.getInstance();
        communicatWithDevices = new CommunicatWithDevices();
        Gson gson = new Gson();
        JsonObject userInput = new JsonParser().parse(String.valueOf(messageFromUnit)).getAsJsonObject(); // User POST Request

        String deviceToChange = "lamp";
        String deviceId = String.valueOf(userInput.get("_id")).replace("\"", "");
        String status = String.valueOf(userInput.get("on")).replace("\"", "");
        Document dbResponse = DBConnector.findDevice(deviceId);

        String deviceToBeChanged = "";
        if (dbResponse != null) {
            deviceToBeChanged = dbResponse.get("device").toString();
            if (deviceToBeChanged.equals("lamp")) {

                Lamp lamp = new Lamp(deviceId, Boolean.parseBoolean((status)));
                smartHouse.addLamp(lamp);
                gson.toJson(smartHouse);

                //socketSession.sendMessage(new TextMessage(communicatWithDevices.changeLampStatus(deviceToChange,deviceId,status)));
                broadcastMessage(gson.toJson("sendRequestToDevice=" + gson.toJson(lamp)));

                return "sendRequestToDevice=" + gson.toJson(lamp);
                //socketSession.sendMessage(new TextMessage((communicatWithDevices.changeLampStatus(deviceToChange, deviceID, status))));
            }
        }
        //broadcastMessage(gson.toJson(communicatWithDevices.changeLampStatus(deviceToChange, deviceId, status)));
        return gson.toJson("");
    }



    public void changeDeviceStatus(String message, String session){

        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject(); // User POST Request
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get("_id")).replace("\"", "");
        if (deviceID.contains(TV)){
            broadcastMessage(message);
        } else {
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
                if (deviceToBeChanged.equals("fan")) {
                    int speed = Integer.parseInt(userInput.get("speed").toString().replace("\"", ""));
                    response = fanHandler(dbResponse, deviceID, speed, userInput);
                }
            }
            Gson gson = new Gson();
            if (response != null) {
                if (response.get("operation").equals("success"))
                    broadcastMessage("changeDeviceStatus=" + gson.toJson(response));
            }
        }
    }

    private String getDeviceStatuses() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        ArrayList<Object> responseMap = new ArrayList<>();
        SmartHouse smartHouse = SmartHouse.getInstance();
        smartHouse.clear();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            String deviceType = (String) article.get("device");
            String id = String.valueOf(article.get("_id"));
            if (deviceType.equals("lamp")) {
                Lamp lamp = new Lamp(id, Boolean.parseBoolean(article.get("on").toString()));
                smartHouse.addLamp(lamp);
            }
            if (deviceType.equals("thermometer")) {
                Thermometer thermometer = new Thermometer(id, Double.parseDouble(article.get("temp").toString()));
                smartHouse.addTemperatureSensor(thermometer);
            }
            if (deviceType.equals("curtain")) {
                Curtain curtain = new Curtain(id, Boolean.parseBoolean(article.get("open").toString()));
                smartHouse.addCurtain(curtain);
            }
            if (deviceType.equals("fan")) {
                Fan fan = new Fan(id, Integer.parseInt(article.get("speed").toString()));
                smartHouse.addFan(fan);
            }
        }
        Gson gson = new Gson();
        return "getDevices=" + gson.toJson(smartHouse);
    }

    private HashMap<String, String> lampHandler(Document dbResponse, String deviceID, String on, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("on").toString().equals(on))) {
                response.put("device", "lamp");
                response.put("_id", deviceID);
                response.put("option", on);
                response.put("operation", "success");
                //We do not need that method any more
                Responder responder = new Responder();
                try {
                    //responder.changeDeviceStatus(deviceID, on, "lamp");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + on);
            }
            return response;
        }
        return null;
    }

    private HashMap<String, String> fanHandler(Document dbResponse, String deviceID, int speed, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("speed").toString().equals(String.valueOf(speed)))) {
                DBConnector.changeDeviceStatus("fan", jsonObject);
                response.put("device", "fan");
                response.put("_id", deviceID);
                response.put("option", String.valueOf(speed));
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + speed);
            }
            return response;
        }
        return null;
    }

    private HashMap<String, String> thermometerHandler(Document dbResponse, String deviceID, String temp, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("temp").toString().equals(temp))) {
                DBConnector.changeDeviceStatus("thermometer", jsonObject);
                response.put("device", "thermometer");
                response.put("_id", deviceID);
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

    private HashMap<String, String> curtainHandler(Document dbResponse, String deviceID, boolean open, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("open").toString().equals(String.valueOf(open)))) {
                DBConnector.changeDeviceStatus("curtain", jsonObject);
                response.put("device", "curtain");
                response.put("_id", deviceID);
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

    private String getTime() {
        Date date = java.util.Calendar.getInstance().getTime();
        return String.valueOf(date);
    }
}