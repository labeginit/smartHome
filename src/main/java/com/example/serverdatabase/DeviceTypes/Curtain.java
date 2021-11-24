package com.example.serverdatabase.DeviceTypes;

public class Curtain {

    String _id;
    boolean status;

    public Curtain(String deviceID, boolean open) {
        _id = deviceID;
        status = open;
    }
}
