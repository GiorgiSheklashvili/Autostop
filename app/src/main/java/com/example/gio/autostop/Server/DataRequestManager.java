package com.example.gio.autostop.server;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.gio.autostop.user_interface.interfaces.MapRequestRequestCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gio on 11/19/2016.
 */

public class DataRequestManager {
    private static DataRequestManager dataRequestManager;
    public static List<Positions> positions = new ArrayList <>();
    private static Positions tempPosition;

    public static DataRequestManager getInstance() {
        if (dataRequestManager == null) dataRequestManager = new DataRequestManager();
        return dataRequestManager;
    }

    private DataRequestManager() {

    }


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

    public static Positions searchList(Double latitude, Double longitude) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getLatitude() == latitude && positions.get(i).getLongitude() == longitude)
                return positions.get(i);
        }
        return null;

    }

//    public MapRequestRequestCallback getCallback() {
//        return callback;
//    }
//
//    public void setCallback(MapRequestRequestCallback callback) {
//        this.callback = callback;
//    }
}
