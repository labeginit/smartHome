package com.example.serverdatabase.DeviceTypes;

public class Thermometer {

    double temperature;
    String deviceID;

    public Thermometer(String deviceID, double temperature) {
        this.deviceID = deviceID;
        this.temperature = temperature;
    }
}
