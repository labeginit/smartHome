package com.example.smarthousewebapp.models;

public class Lamp {


    private boolean lightOn;

    public Lamp(boolean lightOn) {
        this.lightOn = lightOn;
    }

    public Lamp() {
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
    }

    @Override
    public String toString() {
        return "Lamp{" +
                "lightOn=" + lightOn +
                '}';
    }
}
