package com.example.serverdatabase.DeviceTypes;

public class Lamp {

    String deviceID;
    boolean on;

    public Lamp(String deviceID, boolean on) {
        this.deviceID = deviceID;
        this.on = on;
    }

    @Override
    public String toString() {
        return "Lamp{" +
                "deviceID='" + deviceID + '\'' +
                ", on=" + on +
                '}';
    }
}
