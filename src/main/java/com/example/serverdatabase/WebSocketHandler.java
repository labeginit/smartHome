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
    private final static String ID = "_id";
    private final static String STATUS = "status";
    private final static String DEVICE = "device";
    private final static String OPERATION = "operation";
    private final static String SUCCESS = "success";
    private final static String FAILED = "failed";
    private final static String REASON = "reason";

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
        System.out.println("here is the " + OPERATION + ": " + operation);
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
            case ("addDevice"):
                addDevice(jsonData);
                break;
            case ("removeDevice"):
                removeDevice(jsonData);
                break;
            case ("confirmation"):
                getConfirmation(jsonData);
                break;
            case ("addNewDevice"):
                String status = DBConnector.insertNewDoc(jsonData);
                Gson gson = new Gson();
                HashMap<String, String> response = new HashMap<>();
                JsonObject userInput = new JsonParser().parse(jsonData).getAsJsonObject();
                response.put(ID, userInput.get(ID).toString().replace("\"", ""));
                response.put(DEVICE, userInput.get(DEVICE).toString().replace("\"", ""));
                response.put(OPERATION, SUCCESS);
                response.put(STATUS, status);
                broadcastMessage("addNewDevice=" + gson.toJson(response));
                break;
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

        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject();
        HashMap<String, String> response = new HashMap<>();

        String deviceID = String.valueOf(userInput.get(ID)).replace("\"", "");
        if (deviceID.contains(DeviceType.TV.value)) {
            broadcastMessage(message);
            response.put("message", message);
            return response;
        } else {
            Document dbResponse = DBConnector.findDevice(deviceID);
            String deviceToBeChanged;
            if (dbResponse != null) {
                deviceToBeChanged = dbResponse.get(DEVICE).toString();
                String status = String.valueOf(userInput.get(STATUS)).replace("\"", "");

                if (deviceToBeChanged.equals(DeviceType.LAMP.value) ||
                        deviceToBeChanged.equals(DeviceType.THERMOMETER.value) ||
                        deviceToBeChanged.equals(DeviceType.CURTAIN.value) ||
                        deviceToBeChanged.equals(DeviceType.FAN.value) ||
                        deviceToBeChanged.equals(DeviceType.ALARM.value) ||
                        deviceToBeChanged.equals(DeviceType.HEATER.value)) {
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
                if (response.get(OPERATION).equals(SUCCESS))
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
                Alarm alarm = new Alarm(id, Integer.parseInt(article.get(STATUS).toString()));
                smartHouse.addAlarm(alarm);
            }
            if (deviceType.equals(DeviceType.HEATER.value)) {
                Heater heater = new Heater(id, Boolean.parseBoolean(article.get(STATUS).toString()));
                smartHouse.addHeater(heater);
            }
        }
        Gson gson = new Gson();
        return "getDevices=" + gson.toJson(smartHouse);
    }

    private HashMap<String, String> deviceHandler(Document dbResponse, String deviceType, String deviceID, Object status, JsonObject jsonObject) {
        HashMap<String, String> response = new HashMap<>();
        if (dbResponse != null) {
            if ((dbResponse.get(ID).toString().equals(String.valueOf(deviceID)))) {
                if (!(dbResponse.get(STATUS).toString().equals(String.valueOf(status)))) {
                    response.put(DEVICE, deviceType);
                    response.put(ID, deviceID);
                    response.put(STATUS, String.valueOf(status));
                    response.put(OPERATION, SUCCESS);
                } else {
                    response.put(OPERATION, FAILED);
                    response.put(REASON, deviceID + " is already " + status);
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
        return error(deviceID + " contains wrong value", response);
    }

    private HashMap<String, String> error(String reason, HashMap<String, String> response) {
        response.put(OPERATION, FAILED);
        response.put(REASON, reason);
        return response;
    }

    public void getTemp(String message) {
        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject();
        String status = String.valueOf(userInput.get(STATUS)).replace("\"", "");
        String device = String.valueOf(userInput.get(DEVICE)).replace("\"", "");
        String deviceId = String.valueOf(userInput.get(ID)).replace("\"", "");

        Gson gson = new Gson();
        HashMap<String, String> response = new HashMap<>();

        response.put(DEVICE, device);
        response.put(ID, deviceId);
        response.put(STATUS, status);

        System.out.println("Temperature from device:" + userInput);
        broadcastMessage("changeDeviceStatus=" + gson.toJson(response));

    }

    public void getConfirmation(String message) {
        JsonObject userInput = new JsonParser().parse(message).getAsJsonObject();
        String deviceType = String.valueOf(userInput.get(DEVICE)).replace("\"", "");
        String result = String.valueOf(userInput.get("result")).replace("\"", "");
        String deviceID = String.valueOf(userInput.get(ID)).replace("\"", "");
        String status = String.valueOf(userInput.get(STATUS)).replace("\"", "");
        System.out.println(result);

        if (result.equalsIgnoreCase(SUCCESS)) {

            System.out.println("Result:" + result);
            System.out.println("Confirmation from Devices:" + userInput);
            HashMap<String, String> response = new HashMap<>();
            Document dbResponse = DBConnector.findDevice(deviceID);
            Gson gson = new Gson();


            if (dbResponse != null) {
                if ((dbResponse.get(ID).toString().equals(deviceID))) {
                    if (!(dbResponse.get(STATUS).toString().equals(status))) {
                        DBConnector.changeDeviceStatus(deviceType, userInput);
                        response.put(DEVICE, deviceType);
                        response.put(ID, deviceID);
                        response.put(STATUS, status);
                        response.put(OPERATION, SUCCESS);
                    } else {
                        response = error(deviceID + " is already " + status, response);

                    }
                    broadcastMessage("changeDeviceStatus=" + gson.toJson(response));
                }
            } else {
                //The device could not change
                System.out.println("Something went wrong");
                System.out.println(result);
            }

        }

    }

    public void addDevice(String jsonData) {
        JsonObject userInput = new JsonParser().parse(jsonData).getAsJsonObject();
        HashMap<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        String deviceType = String.valueOf(userInput.get(DEVICE)).replace("\"", "");
        String deviceID = String.valueOf(userInput.get(ID)).replace("\"", "");
        String status = String.valueOf(userInput.get(STATUS)).replace("\"", "");
        if (deviceType.equals(DeviceType.LAMP.value) ||
                deviceType.equals(DeviceType.THERMOMETER.value) ||
                deviceType.equals(DeviceType.CURTAIN.value) ||
                deviceType.equals(DeviceType.FAN.value) ||
                deviceType.equals(DeviceType.ALARM.value) ||
                deviceType.equals(DeviceType.HEATER.value)) {
            try {
                DBConnector.insertNewDoc(deviceID, deviceType, status);
                response.put(DEVICE, deviceType);
                response.put(ID, deviceID);
                response.put(STATUS, status);
                response.put(OPERATION, SUCCESS);
            } catch (Exception e) {
                System.out.println("Failed to add the device");
                response = error(deviceID + " already exists", response);
            } finally {
                broadcastMessage("addDevice=" + gson.toJson(response));
            }
        } else broadcastMessage(gson.toJson(error("unknown " + DEVICE, response)));
    }

    public void removeDevice(String jsonData) {
        JsonObject userInput = new JsonParser().parse(jsonData).getAsJsonObject();
        HashMap<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        String deviceID = String.valueOf(userInput.get(ID)).replace("\"", "");
        try {
            DBConnector.removeDoc(deviceID);
            response.put(ID, deviceID);
            response.put(OPERATION, SUCCESS);
        } catch (Exception e) {
            response = error(REASON + " unknown", response);
        } finally {
            broadcastMessage("removeDevice=" + gson.toJson(response));
        }
/*
        JsonObject userRemove = new JsonParser().parse(jsonData).getAsJsonObject();
        Gson removeGson = new Gson();
        DBConnector.removeDoc(userRemove.get("_id").toString().replace("\"", ""));
        HashMap<String, String> removeResponse = new HashMap<>();
        removeResponse.put(ID, userRemove.get(ID).toString().replace("\"", ""));
        removeResponse.put("device", userRemove.get("device").toString().replace("\"", ""));
        removeResponse.put("operation", "success");
        broadcastMessage("removeDevice=" + removeGson.toJson(removeResponse));*/
    }
}