package com.example.serverdatabase.DeviceTypes;

public class Alarm {

    String _id;
    boolean status;

    public Alarm(String deviceID, boolean on) {
        _id = deviceID;
        status = on;
    }

}
