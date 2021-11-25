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
    private final static String ID = "_id";
    private final static String STATUS = "status";
    private final static String DEVICE = "device";
    private CommunicatWithDevices communicatWithDevices;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
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
        String status = String.valueOf(userInput.get("status")).replace("\"", "");
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



    public HashMap<String, String> changeDeviceStatus(String message, String session){

        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject(); // User POST Request
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get(ID)).replace("\"", "");
        if (deviceID.contains(DeviceType.TV.value)){
            broadcastMessage(message);
            response.put("message", message);
            return response;
        } else {
            Document dbResponse = DBConnector.findDevice(deviceID);
            String deviceToBeChanged = "";
            if (dbResponse != null) {
                deviceToBeChanged = dbResponse.get(DEVICE).toString();
                String status = String.valueOf(userInput.get(STATUS)).replace("\"", "");

                if (deviceToBeChanged.equals(DeviceType.LAMP.value) || deviceToBeChanged.equals(DeviceType.THERMOMETER.value) || deviceToBeChanged.equals(DeviceType.CURTAIN.value) || deviceToBeChanged.equals(DeviceType.FAN.value) || deviceToBeChanged.equals(DeviceType.ALARM.value)) {
                    try {
                        response = deviceHandler(dbResponse, deviceToBeChanged,deviceID, status, userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }

                /*
                if (deviceToBeChanged.equals("lamp")) {
                    try {
                        response = lampHandler(dbResponse, deviceID, status, userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }
                if (deviceToBeChanged.equals("thermometer")) {
                    try {
                        response = thermometerHandler(dbResponse, deviceID, status, userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }
                if (deviceToBeChanged.equals("curtain")) {
                    try {
                        response = curtainHandler(dbResponse, deviceID, Boolean.parseBoolean(status), userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }
                if (deviceToBeChanged.equals("fan")) {
                    try {
                        response = fanHandler(dbResponse, deviceID, Integer.parseInt(status), userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }
                if (deviceToBeChanged.equals("alarm")) {
                    try {
                        response = alarmHandler(dbResponse, deviceID, Boolean.parseBoolean(status), userInput);
                    } catch (IllegalArgumentException exception){
                        exception.getSuppressed();
                        return error(response, deviceID);
                    }
                }*/
            } else {
                return error(response, deviceID);
            }
            Gson gson = new Gson();
            if (response != null) {
                if (response.get("operation").equals("success"))
                    broadcastMessage("changeDeviceStatus=" + gson.toJson(response));
            }
            return response;
        }
    }

    protected String getDeviceStatuses() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        ArrayList<Object> responseMap = new ArrayList<>();
        SmartHouse smartHouse = SmartHouse.getInstance();
        smartHouse.clear();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            String deviceType = (String) article.get(DEVICE);
            String id = String.valueOf(article.get(ID));
            if (deviceType.equals(DeviceType.LAMP.value)) {
                Lamp lamp = new Lamp(id, Boolean.parseBoolean(article.get(STATUS).toString()));
                smartHouse.addLamp(lamp);
            }
            if (deviceType.equals(DeviceType.THERMOMETER.value)) {
                Thermometer thermometer = new Thermometer(id, Double.parseDouble(article.get(STATUS).toString()));
                smartHouse.addTemperatureSensor(thermometer);
            }
            if (deviceType.equals(DeviceType.CURTAIN.value)) {
                Curtain curtain = new Curtain(id, Boolean.parseBoolean(article.get(STATUS).toString()));
                smartHouse.addCurtain(curtain);
            }
            if (deviceType.equals(DeviceType.FAN.value)) {
                Fan fan = new Fan(id, Integer.parseInt(article.get(STATUS).toString()));
                smartHouse.addFan(fan);
            }
            if (deviceType.equals(DeviceType.ALARM.value)) {
                Alarm alarm = new Alarm(id, Boolean.parseBoolean(article.get(STATUS).toString()));
                smartHouse.addAlarm(alarm);
            }
        }
        Gson gson = new Gson();
        return "getDevices=" + gson.toJson(smartHouse);
    }
/*
    private HashMap<String, String> lampHandler(Document dbResponse, String deviceID, String on, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("status").toString().equals(on))) {
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
        HashMap<String, String> response = new HashMap<>();
        if (dbResponse != null) {
            if ((dbResponse.get("_id").toString().equals(String.valueOf(deviceID)))) {
                if (!(dbResponse.get("status").toString().equals(String.valueOf(speed)))) {
                    DBConnector.changeDeviceStatus("fan", jsonObject);
                    response.put("device", "fan");
                    response.put("_id", deviceID);
                    response.put("option", String.valueOf(speed));
                    response.put("operation", "success");
                } else {
                    response.put("operation", "failed");
                    response.put("reason", deviceID + " is already " + speed);
                }
            } else {
                response = error(response, deviceID);
            }
        }
        return response;
    }
    */
    private HashMap<String, String> deviceHandler(Document dbResponse, String deviceType, String deviceID, Object status, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        if (dbResponse != null) {
            if ((dbResponse.get(ID).toString().equals(String.valueOf(deviceID)))) {
                if (!(dbResponse.get(STATUS).toString().equals(String.valueOf(status)))) {
                 //   DBConnector.changeDeviceStatus(deviceType, jsonObject);
                    response.put(DEVICE, deviceType);
                    response.put(ID, deviceID);
                    response.put("option", String.valueOf(status));
                    response.put("operation", "success");
                } else {
                    response.put("operation", "failed");
                    response.put("reason", deviceID + " is already " + status);
                }
            } else {
                response = error(response, deviceID);
            }
        }
        return response;
    }
/*
    private HashMap<String, String> thermometerHandler(Document dbResponse, String deviceID, String temp, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        if (dbResponse != null) {
            if ((dbResponse.get("_id").toString().equals(String.valueOf(deviceID)))) {
                if (!(dbResponse.get("status").toString().equals(temp))) {
                    DBConnector.changeDeviceStatus("thermometer", jsonObject);
                    response.put("device", "thermometer");
                    response.put("_id", deviceID);
                    response.put("option", temp);
                    response.put("operation", "success");
                } else {
                    response.put("operation", "failed");
                    response.put("reason", deviceID + " is already " + temp);
                }
            }
             else {
                    response = error(response, deviceID);
                }
        }
        return response;
    }

    private HashMap<String, String> curtainHandler(Document dbResponse, String deviceID, boolean open, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("status").toString().equals(String.valueOf(open)))) {
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

    private HashMap<String, String> alarmHandler(Document dbResponse, String deviceID, boolean status, JsonObject jsonObject) {
        if (dbResponse != null) {
            HashMap<String, String> response = new HashMap<>();
            if (!(dbResponse.get("status").toString().equals(String.valueOf(status)))) {
                response.put("device", "alarm");
                response.put("_id", deviceID);
                response.put("option", String.valueOf(status));
                response.put("operation", "success");
            } else {
                response.put("operation", "failed");
                response.put("reason", deviceID + " is already " + status);
            }
            return response;
        }
        return null;
    }
*/
    private String getTime() {
        Date date = java.util.Calendar.getInstance().getTime();
        return String.valueOf(date);
    }

    private HashMap<String, String> error(HashMap<String, String> response, String deviceID){
        response.put("operation", "failed");
        response.put("reason", deviceID + " contains wrong value");
        return response;
    }
}