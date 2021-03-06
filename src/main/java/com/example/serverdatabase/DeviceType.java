package com.example.serverdatabase;

public enum DeviceType {
    ALARM("alarm"),
    CURTAIN("curtain"),
    FAN("fan"),
    LAMP("lamp"),
    THERMOMETER("thermometer"),
    TV("TV"),
    HEATER("heater");

    public final String value;

    private DeviceType(String value) {
        this.value = value;
    }
}