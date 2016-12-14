package com.example.gio.autostop.Server;


public class Positions {
    private double latitude;
    private double longitude;
    private String mac;
    private String androidId;
    public Positions(double lat,double longT,String mac,String androidId){
this.latitude=lat;
        this.longitude=longT;
        this.mac=mac;
        this.androidId=androidId;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    public String getAndroidId() {
        return androidId;
    }


    public String getMac() {
        return mac;
    }

}
