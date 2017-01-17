package com.example.gio.autostop.user_interface.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.gio.autostop.AutostopSettings;
import com.example.gio.autostop.Constants;
import com.example.gio.autostop.server.DeletePosition;
import com.example.gio.autostop.user_interface.GPSManager;
import com.example.gio.autostop.user_interface.interfaces.GPSCallback;
import com.example.gio.autostop.user_interface.interfaces.MapRequestRequestCallback;
import com.example.gio.autostop.server.Positions;
import com.example.gio.autostop.R;
import com.example.gio.autostop.server.UploadPosition;
import com.example.gio.autostop.user_interface.activities.MapsActivity;
import com.google.android.gms.common.api.GoogleApiClient;
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
    private static AlertDialog alert;
    private static AlertDialog.Builder builder;
    private static Location location;
    private static LatLng newLatLng;
    private static GPSManager gpsManager = null;
    private static int measurement_index = Constants.INDEX_KM;
    private static double speed = 0.0;
    private static LatLng tempDestinationPosition, tempLatLng;
    private static Double lastSpeed = 0.0;
    public static ArrayList<Marker> mMarkerCollection = new ArrayList<>();
    public Button mCheckInButton, mCheckOutButton;
    String myMac, deviceId;
    public static Boolean chosenMode1 = false;
    public  static Context context;
    public static Positions position;
    public static GoogleApiClient googleApiClient1;

    public static void setMarkerForDeletionDestination(Marker marker) {
        markerForDeletionDestination = marker;
    }

    public static void setGoogleApiClient(GoogleApiClient googleApiClient) {
        googleApiClient1 = googleApiClient;
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
    public void onDestroy() {
        if (googleApiClient1.isConnected())
            gpsManagerRemove(gpsManager);
        if(alert!=null)
        {
            alert.dismiss();
            alert=null;
        }


        super.onDestroy();
    }

    public static void gpsManagerRemove(GPSManager gpsManager) {
        if (gpsManager != null) {
            gpsManager.stopListening(googleApiClient1);
            gpsManager.setGPSCallback(null);
            gpsManager = null;
        }

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (markerForDeletion == null)
            AutostopSettings.saveBoolean("carIconAlreadyCreated", false);
        mCheckInButton = (Button) view.findViewById(R.id.button2);
        mCheckOutButton = (Button) view.findViewById(R.id.button3);
        mCheckOutButton.setClickable(AutostopSettings.getBoolean("mCheckOutButton"));
        if (mCheckOutButton.isClickable())
            mCheckInButton.setClickable(false);
        else
            mCheckInButton.setClickable(true);
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutostopSettings.checkGps(getContext());
                final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    checkInCurrentPosition(context);
                    mCheckOutButton.setClickable(true);
                    mCheckInButton.setClickable(false);
                }
            }
        });
        mCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckOutButton.setClickable(false);
                mCheckInButton.setClickable(true);
                AutostopSettings.saveBoolean("mCheckOutButton", false);
                AutostopSettings.saveBoolean("passengerIconAlreadyCreated", false);
                AutostopSettings.saveBoolean("mCheckOutForDriverButton", false);
                deleteMarkers();
                gpsManagerRemove(gpsManager);
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
            AutostopSettings.saveBoolean("mCheckOutButton", false);
        } else {
            AutostopSettings.saveBoolean("mCheckOutForDriverButton", false);
            DriverFragment.unCheckDriver.setClickable(false);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MapFunctionsFragment.context=context;
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
        mCheckOutButton.setClickable(AutostopSettings.getBoolean("mCheckOutButton"));
        if (mCheckOutButton.isClickable())
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
                    if (!kindOfUser) {

                        markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                        AutostopSettings.saveBoolean("passengerIconAlreadyCreated", true);
                    } else {
                        markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                        AutostopSettings.saveBoolean("carIconAlreadyCreated", true);
                    }
                    mMarkerCollection.add(markerForDeletion);
                    AutostopSettings.saveLong("Latitude", Double.doubleToLongBits(lat));
                    AutostopSettings.saveLong("Longitude", Double.doubleToLongBits(lon));
                    tempLatLng = new LatLng(latitudeDestination, longitudeDestination);
                    gpsManagerStart(tempLatLng, kindOfUser);
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (location == null)
            location = getLastKnownLocation(context);
        newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!chosenMode1) {
            if (!AutostopSettings.getBoolean("passengerIconAlreadyCreated")) {
                markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
                markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                AutostopSettings.saveBoolean("passengerIconAlreadyCreated", false);
            }
        } else {
            if (!AutostopSettings.getBoolean("carIconAlreadyCreated")) {
                markerForDeletion = mMapsActivity.mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
                markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                AutostopSettings.saveBoolean("carIconAlreadyCreated", false);
            }
        }
    }

    private static Location getLastKnownLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    public static void uploadingPosition(LatLng destinationPosition, final Boolean chosenMode) {
        if (!chosenMode) {
            AutostopSettings.saveBoolean("passengerIconAlreadyCreated", true);
        }
        AutostopSettings.saveBoolean("mCheckOutButton", true);
        AutostopSettings.saveBoolean("mCheckOutForDriverButton", true);
        chosenMode1 = chosenMode;
        if (chosenMode)
            checkInCurrentPosition(context);
        if (location == null) {
            newLatLng = new LatLng(AutostopSettings.getLong("Latitude"), AutostopSettings.getLong("Longitude"));
        }
        String deviceId = Settings.Secure.getString(mMapsActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
        position = new Positions(newLatLng.latitude, newLatLng.longitude, destinationPosition.latitude, destinationPosition.longitude, chosenMode, getWifiMacAddress(), deviceId);
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
        UploadPosition upload = new UploadPosition(position, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(upload);
        gpsManagerStart(destinationPosition, chosenMode);
    }
    public static void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode) {
        gpsManager = new GPSManager();
        gpsManager.startListening(context, googleApiClient1);
        tempDestinationPosition = destinationPosition;
        gpsManager.setGPSCallback(new GPSCallback() {
            @Override
            public void onGPSUpdate(Location location) {
                location.getLatitude();
                location.getLongitude();
                speed = location.getSpeed();
                Double speedDouble = roundDecimal(convertSpeed(speed), 2);
//                String unitString = measurementUnitString(measurement_index);
                if (speedDouble > Constants.Speed_Margin) {//manqanit mgzavroba
                    if (chosenMode) {
                        lastSpeed = speedDouble;
                        checkInCurrentPosition(context);
                        uploadingPosition(tempDestinationPosition, true);
                        Toast.makeText(context, "driver " + speedDouble.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(context, "mgzavri manqanashi " + speedDouble.toString(), Toast.LENGTH_SHORT).show();

                } else {//pexit mgzavroba
                    if (lastSpeed > 0.0) {
                        checkInReminderAlertMessage(context);
                        Toast.makeText(context, "pexit  " + speedDouble.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private static void checkInReminderAlertMessage(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("You have gone from previous place,it is strongly recommended to check in new position")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
        lastSpeed = 0.0;
    }

    //    private static String measurementUnitString(int unitIndex){
//        String string = "";
//
//        switch(unitIndex)
//        {
//            case Constants.INDEX_KM:		string = "km/h";	break;
//            case Constants.INDEX_MILES:	string = "mi/h";	break;
//        }
//
//        return string;
//    }
    private static double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }

    private static double convertSpeed(double speed) {
        return ((speed * Constants.HOUR_MULTIPLIER) * Constants.UNIT_MULTIPLIERS[measurement_index]);
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
        DeletePosition delete = new DeletePosition(mac, deviceId, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
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
