package com.example.serverdatabase;

public class Singleton {
    private static Singleton TvData;
    private boolean on;
    private int channel;

    private Singleton(){
        on = false;
        channel = 1;
    }

    public static Singleton getInstance(){
        if (TvData == null)
            TvData = new Singleton();
        return TvData;
    }

    public boolean getState(){
        return on;
    }

    public void setState(boolean on){
        this.on = on;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel){
        this.channel = channel;
    }

/*
        Will be replaced with DB requests soon.
        _id: Livingroom  TV
        device: TV
        on: false
        channel: 1
 */

}
