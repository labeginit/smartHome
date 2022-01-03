package com.example.serverdatabase.DeviceTypes;

public class Thermometer {

    double status;
    String _id;

    public Thermometer(String deviceID, double temperature) {
        _id = deviceID;
        status = temperature;
    }
}
