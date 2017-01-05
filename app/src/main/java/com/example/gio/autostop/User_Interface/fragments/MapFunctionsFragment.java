package com.example.gio.autostop.User_Interface.fragments;

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
import com.example.gio.autostop.App;
import com.example.gio.autostop.Server.DeletePosition;
import com.example.gio.autostop.Server.MapRequestRequestCallback;
import com.example.gio.autostop.Server.Positions;
import com.example.gio.autostop.R;
import com.example.gio.autostop.Server.UploadPosition;
import com.example.gio.autostop.User_Interface.activities.MapsActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapFunctionsFragment extends Fragment {
    public static Marker markerForDeletion, markerForDeletionDestination, markerVariable;
    public static LocationManager locationManager;
    private static MapsActivity mMapsActivity;
    private static Location location;
    private static LatLng newLatLng;
    public static ArrayList<Marker> mMarkerCollection = new ArrayList<>();
    public static Button mCheckInButton, mCheckOutButton;
    String myMac, deviceId;
    public static Boolean chosenMode1 = false;
    public static Context context;

    public static void setMarkerForDeletionDestination(Marker marker) {
        markerForDeletionDestination = marker;
    }

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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();
        mCheckInButton = (Button) view.findViewById(R.id.button2);
        mCheckOutButton = (Button) view.findViewById(R.id.button3);
        mCheckOutButton.setClickable(com.example.gio.autostop.Settings.getBoolean("mCheckOutButton"));
        if(mCheckOutButton.isClickable())
            mCheckInButton.setClickable(false);
        else
            mCheckInButton.setClickable(true);
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.gio.autostop.Settings.checkGps(getContext());
                final LocationManager manager = (LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    context = view.getContext();
                    checkInCurrentPosition(view.getContext());
                    mCheckOutButton.setClickable(true);
                    mCheckInButton.setClickable(false);
                }
            }
        });
        mCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.gio.autostop.Settings.saveBoolean("mCheckOutButton", false);
                mCheckOutButton.setClickable(false);
                mCheckInButton.setClickable(true);
                com.example.gio.autostop.Settings.saveBoolean("passengerIconAlreadyCreated",false);
                com.example.gio.autostop.Settings.saveBoolean("mCheckOutForDriverButton", false);
                deleteMarkers();
            }
        });
    }

    public static void deleteMarkers() {
        deletePosition();
        markerForDeletion.remove();
        markerForDeletion = null;
        if (markerForDeletionDestination != null)
            markerForDeletionDestination.remove();
        if (!chosenMode1) {
            com.example.gio.autostop.Settings.saveBoolean("mCheckOutButton", false);
        } else {
            com.example.gio.autostop.Settings.saveBoolean("mCheckOutForDriverButton", false);
            DriverFragment.unCheckDriver.setClickable(false);
        }

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

    @Override
    public void onResume() {
        super.onResume();
        mCheckOutButton.setClickable(com.example.gio.autostop.Settings.getBoolean("mCheckOutButton"));
        if(mCheckOutButton.isClickable())
            mCheckInButton.setClickable(false);
        else
            mCheckInButton.setClickable(true);
    }

    public static boolean isMarkerForDeletionInitialized() {
        return markerForDeletion != null;
    }

    public MapRequestRequestCallback callback = new MapRequestRequestCallback() {
        @Override
        public void onRequestedLoaded(double lat, double lon, String mac, String android_id, double latitudeDestination, double longitudeDestination, Boolean kindOfUser) {
            deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
            myMac = getWifiMacAddress();
            if (!isMarkerOnArray(mMarkerCollection, lat, lon) || (myMac.equals(mac) && deviceId.equals(android_id)))
                if (myMac.equals(mac) && deviceId.equals(android_id)) {
                    markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
                    if (!kindOfUser){
                        markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                        com.example.gio.autostop.Settings.saveBoolean("passengerIconAlreadyCreated",true);
                    }
                    else{
                        markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                        com.example.gio.autostop.Settings.saveBoolean("carIconAlreadyCreated",true);
                    }
                    mMarkerCollection.add(markerForDeletion);
                    com.example.gio.autostop.Settings.saveLong("Latitude", Double.doubleToLongBits(lat));
                    com.example.gio.autostop.Settings.saveLong("Longitude", Double.doubleToLongBits(lon));
                } else {
                    markerVariable = mMapsActivity.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
                    if (!kindOfUser)
                        markerVariable.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                    else
                        markerVariable.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                    mMarkerCollection.add(markerVariable);
                }
        }
    };

    public static void checkInCurrentPosition(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
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
//        assert location != null;
        newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!chosenMode1) {
            if(!com.example.gio.autostop.Settings.getBoolean("passengerIconAlreadyCreated")){
            markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
            markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                com.example.gio.autostop.Settings.saveBoolean("passengerIconAlreadyCreated",false);
            }
        } else {
            if(!com.example.gio.autostop.Settings.getBoolean("carIconAlreadyCreated")){
            markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
            markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                com.example.gio.autostop.Settings.saveBoolean("carIconAlreadyCreated",false);
            }
        }
    }

    public static void uploadingPosition(LatLng destinationPosition, Boolean chosenMode) {
        if(!chosenMode){
        com.example.gio.autostop.Settings.saveBoolean("passengerIconAlreadyCreated",true);
        }
        com.example.gio.autostop.Settings.saveBoolean("mCheckOutButton", true);
        chosenMode1 = chosenMode;
        if (chosenMode)
            checkInCurrentPosition(App.getAppContext());
        if (location == null){
            newLatLng = new LatLng(com.example.gio.autostop.Settings.getLong("Latitude"), com.example.gio.autostop.Settings.getLong("Longitude"));
        }
            String deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        Positions position = new Positions(newLatLng.latitude, newLatLng.longitude, destinationPosition.latitude, destinationPosition.longitude, chosenMode, getWifiMacAddress(), deviceId);
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(App.getAppContext());
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
        RequestQueue queue = Volley.newRequestQueue(App.getAppContext());
        queue.add(upload);
    }

    public static void deletePosition() {
        String deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac = getWifiMacAddress();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse = new JSONObject(s);
                    boolean success = jsonResponse.getBoolean("success");
                    if (!success) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(App.getAppContext());
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
        RequestQueue queue = Volley.newRequestQueue(App.getAppContext());
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

    private static boolean isMarkerOnArray(ArrayList<Marker> array, Double Latitude, Double Longitude) {
        Marker current;
        for (int c = 0; c < array.size(); c++) {
            current = array.get(c);
            if ((current.getPosition().latitude == Latitude) && (current.getPosition().longitude == Longitude))
                return true;
        }
        return false;
    }


}
