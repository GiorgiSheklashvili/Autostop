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
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInCurrentPosition();
                mCheckInButton.setClickable(false);
                mCheckOutButton.setClickable(true);
            }
        });
        mCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePosition();
                markerForDeletion.remove();
                mCheckOutButton.setClickable(false);
                mCheckInButton.setClickable(true);
            }
        });
        mCheckOutButton.setClickable(false);
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
                            if (!isMarkerOnArray(mMarkerCollection, latitude, longitude))
                                mMarkerCollection.add(mMapsActivity.mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))));
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
