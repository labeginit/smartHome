package com.example.serverdatabase.DeviceConnector;
import com.example.serverdatabase.DeviceTypes.*;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
public class CommunicatWithDevices {
    private String message;
    private JSONObject json;

    public String changeLampStatus(String deviceType, String deviceId, String status) throws JSONException {
        SmartHouse smartHouse = SmartHouse.getInstance();
        Lamp lamp = new Lamp(deviceId, Boolean.parseBoolean((status)));
        smartHouse.addLamp(lamp);
        Gson gson = new Gson();
        gson.toJson(smartHouse);

        return "sendRequestToDevice=" + gson.toJson(smartHouse);
    }

    public String changeFanSpeed(String deviceType, String deviceId, int speed) throws JSONException {

        SmartHouse smartHouse = SmartHouse.getInstance();
        Fan fan = new Fan(deviceId, speed);
        smartHouse.addFan(fan);
        Gson gson = new Gson();
        gson.toJson(smartHouse);

        return "sendRequestToDevice=" + gson.toJson(smartHouse);

    }

    public String changeThermometer(String deviceType, String deviceId, double temp) throws JSONException {


        SmartHouse smartHouse = SmartHouse.getInstance();
        Thermometer thermometer = new Thermometer(deviceId, temp);
        smartHouse.addTemperatureSensor(thermometer);
        Gson gson = new Gson();
        gson.toJson(smartHouse);
        return "sendRequestToDevice=" + gson.toJson(smartHouse);

    }

    public String changeCurtainStatus(String deviceType, String deviceId, boolean status) throws JSONException {

        SmartHouse smartHouse = SmartHouse.getInstance();
        Curtain curtain = new Curtain(deviceId, status);
        smartHouse.addCurtain(curtain);
        Gson gson = new Gson();
        gson.toJson(smartHouse);

        return "sendRequestToDevice=" + gson.toJson(smartHouse);

    }

}
