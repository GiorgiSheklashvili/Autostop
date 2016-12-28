package com.example.gio.autostop.Server;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gio on 11/19/2016.
 */

public class DataRequestManager {
    private static DataRequestManager dataRequestManager;

    public static DataRequestManager getInstance(){
        if(dataRequestManager== null) dataRequestManager = new DataRequestManager();
        return dataRequestManager;
    }

    private DataRequestManager(){

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
                            Double longitudeDestination = jsonObject.getDouble("longitudeDestination");
                            Boolean kindOfUser=jsonObject.getBoolean("kindOfUser");
                            callback.onRequestedLoaded(latitude, longitude,mac,android_id,latitudeDestination,longitudeDestination,kindOfUser);
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


//    public MapRequestRequestCallback getCallback() {
//        return callback;
//    }
//
//    public void setCallback(MapRequestRequestCallback callback) {
//        this.callback = callback;
//    }
}
