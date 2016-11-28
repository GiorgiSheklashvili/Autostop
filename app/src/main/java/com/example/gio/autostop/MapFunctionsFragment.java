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
import android.util.Log;
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

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class MapFunctionsFragment extends Fragment {
    public Marker markerForDeletion;
    public LocationManager locationManager;
    private MapsActivity mMapsActivity;
    private ArrayList<Marker> mMarkerCollection = new ArrayList<>();
    private Button mCheckInButton, mCheckOutButton;

    public MapFunctionsFragment() {
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
        mCheckInButton = (Button) view.findViewById(R.id.button2);
        mCheckOutButton = (Button) view.findViewById(R.id.button3);
        mCheckOutButton.setClickable(com.example.gio.autostop.Settings.getBoolean("mCheckOutButton"));
        mCheckInButton.setClickable(com.example.gio.autostop.Settings.getBoolean("mCheckInButton"));
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInCurrentPosition();
                com.example.gio.autostop.Settings.saveBoolean("mCheckInButton",false);
                com.example.gio.autostop.Settings.saveBoolean("mCheckOutButton",true);
                mCheckInButton.setClickable(false);
                mCheckOutButton.setClickable(true);
            }
        });
        mCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePosition();

                markerForDeletion.remove();
                com.example.gio.autostop.Settings.saveBoolean("mCheckOutButton",false);
                com.example.gio.autostop.Settings.saveBoolean("mCheckInButton",true);
                mCheckOutButton.setClickable(false);
                mCheckInButton.setClickable(true);
            }
        });
//        mCheckOutButton.setClickable(false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setMapsActivity(MapsActivity mMapsActivity) {
        this.mMapsActivity = mMapsActivity;

    }


    public MapRequestRequestCallback callback = new MapRequestRequestCallback() {
        @Override
        public void onRequestedLoaded(double lon, double lat) {
            if (!isMarkerOnArray(mMarkerCollection, lat, lon))
                mMarkerCollection.add(mMapsActivity.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))));

        }
    };

    public void checkInCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
        markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
        com.example.gio.autostop.Settings.saveLong("Latitude",Double.doubleToLongBits(location.getLatitude()));
        com.example.gio.autostop.Settings.saveLong("Longitude",Double.doubleToLongBits(location.getLongitude()));
        String deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        Positions position = new Positions(newLatLng.latitude, newLatLng.longitude, getWifiMacAddress(), deviceId);
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

    public void deletePosition() {
        String deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac = getWifiMacAddress();
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

    public static String getWifiMacAddress() {
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
