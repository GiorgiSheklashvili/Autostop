package com.example.gio.autostop.model;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.gio.autostop.MVP_Interfaces;
import com.example.gio.autostop.helper.AutostopSettings;
import com.example.gio.autostop.server.DeletePositionRequest;
import com.example.gio.autostop.server.DownloadPositionRequest;
import com.example.gio.autostop.server.UploadPositionRequest;
import com.example.gio.autostop.interfaces.MapRequestRequestCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapModel implements MVP_Interfaces.ProvidedModelOps {
    private MVP_Interfaces.RequiredPresenterOps mPresenter;
    public List<Position> positionList = new ArrayList<>();
    private Position tempPosition;
    private Location location;
    public LatLng newLatLng;
    private AlertDialog.Builder builder;
    private Position position;
    public JSONArray jsonArray;
    public String deviceId;

    public MapModel() {

    }

    public MapModel(MVP_Interfaces.RequiredPresenterOps presenter) {
        this.mPresenter = presenter;
    }

    public void setPresenter(MVP_Interfaces.RequiredPresenterOps presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public LatLng checkInCurrentPosition(final Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                FirebaseCrash.log("permission is not granted from checkin");
                FirebaseCrash.report(new Exception("permission is not granted from checkin"));
                return null;
            }
            if (location == null) {
                location = mPresenter.getLastKnownLocation(context);
            }
            if (location == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Check In Failed")
                        .setPositiveButton("retry", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkInCurrentPosition(context);
                            }
                        })
                        .setNegativeButton("close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
            newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ex) {
            FirebaseCrash.log("location is null");
            FirebaseCrash.report(ex);

        }
        return newLatLng;
    }

    @Override
    public void setUpMap(final Context context, final MapRequestRequestCallback callback) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        jsonArray = jsonResponse.getJSONArray("data");
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
                            tempPosition = new Position(latitude, longitude, latitudeDestination, longitudeDestination, kindOfUser, mac, android_id);
                            positionList.add(tempPosition);
                            callback.onRequestedLoaded(latitude, longitude, mac, android_id, latitudeDestination, longitudeDestination, kindOfUser);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        DownloadPositionRequest downloadPosition = new DownloadPositionRequest(responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(downloadPosition);
    }

    public Position searchList(Double latitude, Double longitude) {
        for (int i = 0; i < positionList.size(); i++) {
            if (positionList.get(i).getLatitude() == latitude && positionList.get(i).getLongitude() == longitude)
                return positionList.get(i);
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
    public void deleteMarkers(Marker markerForDeletion, final Context context) {
        markerForDeletion.remove();
        markerForDeletion = null;
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac = getWifiMacAddress();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        DeletePositionRequest delete = new DeletePositionRequest(mac, deviceId, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(delete);
        mPresenter.notifyDeleteDMarkers();
    }

    @Override
    public void uploadingPosition(final Context context, LatLng destinationPosition, Boolean chosenMode) {
        if (!chosenMode) {
            AutostopSettings.saveBoolean("passengerIconAlreadyCreated", true);
        } else {
            AutostopSettings.saveBoolean("carIconAlreadyCreated", true);
        }
        AutostopSettings.saveBoolean("mCheckOutButton", true);
//        mChosenMode = chosenMode;
        if (location == null) {
            newLatLng = new LatLng(AutostopSettings.getLong("Latitude"), AutostopSettings.getLong("Longitude"));
        }
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        position = new Position(newLatLng.latitude, newLatLng.longitude, destinationPosition.latitude, destinationPosition.longitude, chosenMode, getWifiMacAddress(), deviceId);
        positionList.add(position);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        builder = new AlertDialog.Builder(context);
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
        UploadPositionRequest upload = new UploadPositionRequest(position, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
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
