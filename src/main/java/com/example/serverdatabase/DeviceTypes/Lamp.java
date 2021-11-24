package com.example.serverdatabase.DeviceTypes;

public class Lamp {

    String _id;
    boolean status;

    public Lamp(String deviceID, boolean on) {
        _id = deviceID;
        status = on;
    }

    @Override
    public String toString() {
        return "Lamp{" +
                "_id='" + _id + '\'' +
                ", status=" + status +
                '}';
    }
}
