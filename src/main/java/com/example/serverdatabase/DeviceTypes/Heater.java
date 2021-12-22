package com.example.serverdatabase.DeviceTypes;

public class Heater {
    private String _id;
    private boolean status;

    public Heater(String _id, boolean status) {
        this._id = _id;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Heater{" +
                "_id='" + _id + '\'' +
                ", status=" + status +
                '}';
    }
}
