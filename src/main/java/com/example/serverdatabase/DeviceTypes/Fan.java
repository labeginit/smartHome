package com.example.serverdatabase.DeviceTypes;

public class Fan {

    private String _id;
    private int status;

    public Fan(String deviceID, int speed) {
        _id = deviceID;
        status = speed;
    }

    public String getdeviceID() {
        return _id;
    }

    public int getSpeed() {
        return status;
    }

    public void setdeviceID(String deviceID) {
        _id = deviceID;
    }

    public void setSpeed(int speed) {
        status = speed;
    }
}
