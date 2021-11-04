package com.example.serverdatabase.DeviceTypes;

public class Fan {

    private String deviceID;
    private int speed;

    public Fan(String deviceID, int speed) {
        this.deviceID = deviceID;
        this.speed = speed;
    }

    public String getdeviceID() {
        return deviceID;
    }

    public int getSpeed() {
        return speed;
    }

    public void setdeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
