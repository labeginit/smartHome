package com.example.serverdatabase;

import com.example.serverdatabase.DeviceTypes.*;
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
import java.util.Date;
import java.util.HashMap;

public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final ArrayList<WebSocketSession> clients = new ArrayList<>();

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
                changeDeviceStatus(jsonData);
                break;
            case ("temperature"):
                getTemp(jsonData);
                break;
            case ("confirmation"):
                getConfirmation(jsonData);
                break;
            case ("addNewDevice"):
                String status = DBConnector.insertNewDoc(jsonData);
                Gson gson = new Gson();
                HashMap<String, String> response = new HashMap<>();
                JsonObject userInput = new JsonParser().parse(jsonData).getAsJsonObject();
                response.put("_id", userInput.get("_id").toString().replace("\"", ""));
                response.put("device", userInput.get("device").toString().replace("\"", ""));
                response.put("operation", "success");
                response.put("status", status);
                broadcastMessage("addNewDevice=" + gson.toJson(response));
                break;
            case ("removeDevice"):
                JsonObject userRemove = new JsonParser().parse(jsonData).getAsJsonObject();
                Gson removeGson = new Gson();
                DBConnector.removeDoc(userRemove.get("_id").toString().replace("\"", ""));
                HashMap<String, String> removeResponse = new HashMap<>();
                removeResponse.put("_id", userRemove.get("_id").toString().replace("\"", ""));
                removeResponse.put("device", userRemove.get("device").toString().replace("\"", ""));
                removeResponse.put("operation", "success");
                broadcastMessage("removeDevice=" + removeGson.toJson(removeResponse));

            default:
                System.out.println("Connected to Client");
        }
    }

    private void broadcastMessage(String message) {
        for (WebSocketSession cl : clients) {
            try {
                if (cl.isOpen())
                    cl.sendMessage(new TextMessage(message));
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

    public HashMap<String, String> changeDeviceStatus(String message) {

        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject(); // User POST Request
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get("_id")).replace("\"", "");
        if (deviceID.contains(DeviceType.TV.value)) {
            broadcastMessage(message);
            response.put("message", message);
            return response;
        } else {
            Document dbResponse = DBConnector.findDevice(deviceID);
            String deviceToBeChanged;
            if (dbResponse != null) {
                deviceToBeChanged = dbResponse.get("device").toString();
                String status = String.valueOf(userInput.get("status")).replace("\"", "");

                if (deviceToBeChanged.equals(DeviceType.LAMP.value) || deviceToBeChanged.equals(DeviceType.THERMOMETER.value) || deviceToBeChanged.equals(DeviceType.CURTAIN.value) || deviceToBeChanged.equals(DeviceType.FAN.value) || deviceToBeChanged.equals(DeviceType.ALARM.value)) {
                    try {
                        response = deviceHandler(dbResponse, deviceToBeChanged, deviceID, status, userInput);
                    } catch (IllegalArgumentException exception) {
                        exception.printStackTrace();
                        return error(response, deviceID);
                    }
                }
            } else {
                return error(response, deviceID);
            }
            Gson gson = new Gson();
            if (response != null) {
                if (response.get("operation").equals("success"))
                    broadcastMessage("changeDeviceStatus=" + gson.toJson(response));
                broadcastMessage("changeDeviceStatus2Device=" + gson.toJson(response));
            }
            return response;
        }
    }

    protected String getDeviceStatuses() {
        MongoCursor<Document> cursor = DBConnector.collection.find().iterator();
        SmartHouse smartHouse = SmartHouse.getInstance();
        smartHouse.clear();
        while (cursor.hasNext()) {
            Document article = cursor.next();
            String deviceType = (String) article.get("device");
            String id = String.valueOf(article.get("_id"));
            if (deviceType.equals(DeviceType.LAMP.value)) {
                Lamp lamp = new Lamp(id, Boolean.parseBoolean(article.get("status").toString()));
                smartHouse.addLamp(lamp);
            }
            if (deviceType.equals(DeviceType.THERMOMETER.value)) {
                Thermometer thermometer = new Thermometer(id, Double.parseDouble(article.get("status").toString()));
                smartHouse.addTemperatureSensor(thermometer);
            }
            if (deviceType.equals(DeviceType.CURTAIN.value)) {
                Curtain curtain = new Curtain(id, Boolean.parseBoolean(article.get("status").toString()));
                smartHouse.addCurtain(curtain);
            }
            if (deviceType.equals(DeviceType.FAN.value)) {
                Fan fan = new Fan(id, Integer.parseInt(article.get("status").toString()));
                smartHouse.addFan(fan);
            }
            if (deviceType.equals(DeviceType.ALARM.value)) {
                Alarm alarm = new Alarm(id, Integer.parseInt(article.get("status").toString()));
                smartHouse.addAlarm(alarm);
            }
        }
        Gson gson = new Gson();
        return "getDevices=" + gson.toJson(smartHouse);
    }

    private HashMap<String, String> deviceHandler(Document dbResponse, String deviceType, String deviceID, Object status, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        if (dbResponse != null) {
            if ((dbResponse.get("_id").toString().equals(String.valueOf(deviceID)))) {
                if (!(dbResponse.get("status").toString().equals(String.valueOf(status)))) {
                    //   DBConnector.changeDeviceStatus(deviceType, jsonObject);
                    response.put("device", deviceType);
                    response.put("_id", deviceID);
                    response.put("status", String.valueOf(status));
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

    private String getTime() {
        Date date = java.util.Calendar.getInstance().getTime();
        return String.valueOf(date);
    }

    private HashMap<String, String> error(HashMap<String, String> response, String deviceID) {
        response.put("operation", "failed");
        response.put("reason", deviceID + " contains wrong value");
        return response;
    }

    public void getTemp(String message) {
        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject();
        String status = String.valueOf(userInput.get("status")).replace("\"", "");
        String device = String.valueOf(userInput.get("device")).replace("\"", "");
        String deviceId = String.valueOf(userInput.get("_id")).replace("\"", "");

        Gson gson = new Gson();
        HashMap<String, String> response = new HashMap<>();

        response.put("device", device);
        response.put("_id", deviceId);
        response.put("status", status);

        System.out.println("Temperature from device:" + userInput);
        broadcastMessage("changeDeviceStatus=" + gson.toJson(response));

    }

    public void getConfirmation(String message) {
        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject();
        String deviceType = String.valueOf(userInput.get("device")).replace("\"", "");
        String result = String.valueOf(userInput.get("result")).replace("\"", "");
        String deviceID = String.valueOf(userInput.get("_id")).replace("\"", "");
        String status = String.valueOf(userInput.get("status")).replace("\"", "");
        System.out.println(result);

        if (result.equalsIgnoreCase("success")) {

            System.out.println("Result:" + result);
            System.out.println("Confirmation from Devices:" + userInput);
            HashMap<String, String> response = new HashMap<>();
            Document dbResponse = DBConnector.findDevice(deviceID);
            Gson gson = new Gson();


            if (dbResponse != null) {
                if ((dbResponse.get("_id").toString().equals(deviceID))) {
                    if (!(dbResponse.get("status").toString().equals(status))) {
                        DBConnector.changeDeviceStatus(deviceType, userInput);
                        response.put("device", deviceType);
                        response.put("_id", deviceID);
                        response.put("status", status);
                        response.put("operation", "success");
                    } else {
                        response.put("operation", "failed");
                        response.put("reason", deviceID + " is already " + status);


                    }
                    broadcastMessage("changeDeviceStatus=" + gson.toJson(response));
                }
            } else {
                //The device could not change
                System.out.println("Something went");
                System.out.println(result);
            }

        }

    }
}