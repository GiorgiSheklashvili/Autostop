package com.example.gio.autostop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MapFunctions extends Fragment {
    public Marker markerForDeletion;
    private  MapsActivity mapsActivity;
    private ArrayList<Marker> markerCollection = new ArrayList<>();
    private Button checkInButton, checkOutButton;
    public LocationManager mLocationManager;
    public MapFunctions() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_functions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkInButton = (Button) view.findViewById(R.id.button2);
        checkOutButton = (Button) view.findViewById(R.id.button3);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInCurrentPosition();
                checkInButton.setClickable(false);
                checkOutButton.setClickable(true);
            }
        });
        checkOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePosition();
                markerForDeletion.remove();
                checkOutButton.setClickable(false);
                checkInButton.setClickable(true);
            }
        });
        checkOutButton.setClickable(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    public void setMapsActivity(MapsActivity mapsActivity){
        this.mapsActivity=mapsActivity;

    }

    public void setUpMap() {
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
                            if (!isMarkerOnArray(markerCollection, latitude, longitude))
                                markerCollection.add(mapsActivity.mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))));
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(downloadPosition);
    }

    public void deletePosition() {
        String deviceId = Settings.Secure.getString(mapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac = mapsActivity.getWifiMacAddress();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        DeletePosition delete = new DeletePosition(mac, deviceId, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(delete);
    }

    public void checkInCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location location;
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
        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerForDeletion = mapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
        String deviceId = Settings.Secure.getString(mapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        Positions position = new Positions(newLatLng.latitude, newLatLng.longitude, mapsActivity.getWifiMacAddress(), deviceId);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(upload);

    }

    private boolean isMarkerOnArray(ArrayList<Marker> array, Double Latitude, Double Longitude) {
        Marker current;
        for (int c = 0; c < array.size(); c++) {
            current = array.get(c);
            if ((current.getPosition().latitude == Latitude) && (current.getPosition().longitude == Longitude))
                return true;
        }
        return false;
    }


}
