package com.example.gio.autostop.Server;


public class Positions {
    private double latitude;
    private double longitude;
    private String mac;
    private String androidId;
    private double latitudeDestination;
    private double longitudeDestination;
    private Boolean kindOfUser;

    public Positions(double lat, double longT, double latitudeDestination, double longitudeDestination, boolean kindOfUser, String mac, String androidId) {
        this.latitude = lat;
        this.longitude = longT;
        this.mac = mac;
        this.androidId = androidId;
        this.latitudeDestination = latitudeDestination;
        this.longitudeDestination = longitudeDestination;
        this.kindOfUser = kindOfUser;
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

    public double getLatitudeDestination() {
        return latitudeDestination;
    }

    public double getLongitudeDestination() {
        return longitudeDestination;
    }

    public Boolean getIsKindOfUser() {
        return kindOfUser;
    }
}
