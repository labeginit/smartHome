package com.example.serverdatabase;

public class Singleton {
    private static Singleton TvData;
    private boolean state;
    private int channel;

    private Singleton(){
        state = false;
        channel = 1;
    }

    public static Singleton getInstance(){
        if (TvData == null)
            TvData = new Singleton();
        return TvData;
    }

    public boolean getState(){
        return state;
    }

    public void setState(boolean state){
        this.state = state;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel){
        this.channel = channel;
    }



}
