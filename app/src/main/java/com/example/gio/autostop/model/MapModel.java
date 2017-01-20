package com.example.gio.autostop.model;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.gio.autostop.MVP_Interfaces;
import com.example.gio.autostop.helper.AutostopSettings;
import com.example.gio.autostop.server.DeletePosition;
import com.example.gio.autostop.server.DownloadPosition;
import com.example.gio.autostop.server.Positions;
import com.example.gio.autostop.server.UploadPosition;
import com.example.gio.autostop.interfaces.MapRequestRequestCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapModel implements MVP_Interfaces.ProvidedModelOps {
    private MVP_Interfaces.RequiredPresenterOps mPresenter;
    private LocationManager locationManager;
    private List<Positions> positions = new ArrayList<>();
    private Positions tempPosition;
    private Location location;
    private LatLng newLatLng;
    private AlertDialog.Builder builder;
    private Positions position;

    public MapModel(MVP_Interfaces.RequiredPresenterOps presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public LatLng checkInCurrentPosition(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }
        long NetLocationTime = 0;
        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }
        if (0 < GPSLocationTime - NetLocationTime) {
            location = locationGPS;
        } else {
            location = locationNet;
        }
        if (location == null)
            location = mPresenter.getLastKnownLocation(context);
        newLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        return newLatLng;
    }

    @Override
    public void setUpMap(final Activity activity, final MapRequestRequestCallback callback) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonResponse.getJSONArray("data");
                        JSONObject jsonObject;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            String mac = jsonObject.getString("mac");
                            String android_id = jsonObject.getString("android_id");
                            Double latitude = jsonObject.getDouble("latitude");
                            Double longitude = jsonObject.getDouble("longitude");
                            Double latitudeDestination = jsonObject.getDouble("latitudeDestination");
                            Boolean kindOfUser = Boolean.valueOf(jsonObject.getString("kindOfUser"));
                            Double longitudeDestination = jsonObject.getDouble("longitudeDestination");
                            tempPosition = new Positions(latitude, longitude, latitudeDestination, longitudeDestination, kindOfUser, mac, android_id);
                            positions.add(tempPosition);
                            callback.onRequestedLoaded(latitude, longitude, mac, android_id, latitudeDestination, longitudeDestination, kindOfUser);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("Downloading position failed")
                                .setNegativeButton("retry", null)
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        DownloadPosition downloadPosition = new DownloadPosition(responseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(downloadPosition);
    }

    public Positions searchList(Double latitude, Double longitude) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getLatitude() == latitude && positions.get(i).getLongitude() == longitude)
                return positions.get(i);
        }
        return null;

    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            mPresenter = null;
        }
    }

    @Override
    public void deleteMarkers(Marker markerForDeletion, final Activity activity) {
        markerForDeletion.remove();
        markerForDeletion = null;
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac = getWifiMacAddress();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("deleting position failed")
                                .setNegativeButton("retry", null)
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        DeletePosition delete = new DeletePosition(mac, deviceId, responseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(delete);
        mPresenter.notifyDeleteDMarkers();
    }

    @Override
    public void uploadingPosition(final Activity activity, LatLng destinationPosition, Boolean chosenMode) {
        if (!chosenMode) {
            AutostopSettings.saveBoolean("passengerIconAlreadyCreated", true);
        }
        AutostopSettings.saveBoolean("mCheckOutButton", true);
        AutostopSettings.saveBoolean("mCheckOutForDriverButton", true);
//        mChosenMode = chosenMode;
        if (location == null) {
            newLatLng = new LatLng(AutostopSettings.getLong("Latitude"), AutostopSettings.getLong("Longitude"));
        }
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        position = new Positions(newLatLng.latitude, newLatLng.longitude, destinationPosition.latitude, destinationPosition.longitude, chosenMode, getWifiMacAddress(), deviceId);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        builder = new AlertDialog.Builder(activity);
                        builder.setMessage("uploading position failed")
                                .setNegativeButton("retry", null)
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        UploadPosition upload = new UploadPosition(position, responseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(upload);
        mPresenter.gpsManagerStart(destinationPosition, chosenMode);
    }
    @Override
    public String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            Log.i("getWifiMacAddress", "exception in getWifiMacAddress");
        }
        return "";
    }
}
