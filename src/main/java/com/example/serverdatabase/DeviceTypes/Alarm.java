package com.example.serverdatabase.DeviceTypes;

public class Alarm {

    String _id;
    int status;

    public Alarm(String deviceID, int on) {
        _id = deviceID;
        status = on;
    }

}
