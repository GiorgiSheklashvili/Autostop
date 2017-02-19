package com.home.gio.autostop.helper;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.home.gio.autostop.App;
import com.home.gio.autostop.interfaces.GPSCallback;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class GPSManager {
    private final int gpsMinTime = 60*1000;
    private final int gpsMinDistance = 0;
    protected LocationRequest mLocationRequest;
//    private static LocationManager locationManager = null;
    private static LocationListener locationListener = null;
    private static GPSCallback gpsCallback = null;

    public GPSManager() {
        GPSManager.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (GPSManager.gpsCallback != null) {
                    GPSManager.gpsCallback.onGPSUpdate(location);
                }
            }
        } ;
    }

    public GPSCallback getGPSCallback() {
        return GPSManager.gpsCallback;
    }

    public void setGPSCallback(final GPSCallback gpsCallback) {
        GPSManager.gpsCallback = gpsCallback;
    }

    public void startListening(final Context context, GoogleApiClient googleApiClient) {
        createLocationRequest();
            if (ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,mLocationRequest,GPSManager.locationListener);
        }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(gpsMinTime);
        mLocationRequest.setFastestInterval(gpsMinTime);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(gpsMinDistance);
    }
    public void stopListening(GoogleApiClient googleApiClient) {
//        try {
//            if (GPSManager.locationManager != null && GPSManager.locationListener != null) {
                if (ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
//                GPSManager.locationManager.removeUpdates(GPSManager.locationListener);
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, GPSManager.locationListener);


//            GPSManager.locationManager = null;
//         catch (final Exception ex) {


    }

}

