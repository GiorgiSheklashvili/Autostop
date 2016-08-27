package com.example.gio.autostop;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;


import java.net.NetworkInterface;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks {


    public int permissionRequestCounter;
    public GoogleApiClient mGoogleApiClient;
    public Boolean startedLocationUpdate;
    public LocationRequest locationRequest;
    public Location mCurrentLocation;
    public final static int MILISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    public final static int MINUTE = 60 * MILISECONDS_PER_SECOND;
    protected String mLastUpdateTime;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected static final String TAG = "main-activity";
    public AddressFragment AddressFragment;
    public MapFunctions mapFunctions;
    public GoogleMap mMap;// Might be null if Google Play services APK is not available.




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
//        if (savedInstanceState == null) {
//            AddressFragment addressFragment1 = new AddressFragment();
//            addressFragment1.setArguments(getIntent().getExtras());
//            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, addressFragment1).commit();
//        }
        AddressFragment = (AddressFragment) getSupportFragmentManager().findFragmentById(R.id.AddressFragment);
        AddressFragment.setMapsActivity(this);
        mapFunctions=(MapFunctions) getSupportFragmentManager().findFragmentById(R.id.MapFunctions);
        mapFunctions.setMapsActivity(this);
        startedLocationUpdate = false;
        permissionRequestCounter = 0;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILISECONDS_PER_SECOND);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            checkGps();
        }

        deviceUniqueNumber();
        updateValuesFromBundle(savedInstanceState);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        if (mGoogleApiClient.isConnected() && !startedLocationUpdate)
            startLocationUpdates();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (!startedLocationUpdate)
            startLocationUpdates();
        if (mCurrentLocation != null) {

            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_SHORT).show();
                return;
            }
            if (AddressFragment.mAddressRequested) {
                AddressFragment.startIntentService();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        mGoogleApiClient.disconnect();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
            if (mMap != null) {
                mapFunctions.setUpMap();
            }
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        startedLocationUpdate = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() && startedLocationUpdate)
            stopLocationUpdates();

    }


    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MapsActivity.this);
                startedLocationUpdate = true;
            } else {
                if (permissionRequestCounter == 0) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    permissionRequestCounter++;
                }
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        mapFunctions.setUpMap();
    }

    public void enableMyLocation() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                if (permissionRequestCounter == 0) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    permissionRequestCounter++;
                }
            } else if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    checkGps();
                } else {
                    Toast.makeText(MapsActivity.this, "Permission was blocked", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        checkGps();
        return false;
    }

    public void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        AddressFragment.fetchAddressHandler();
    }




    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, startedLocationUpdate);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
                startedLocationUpdate = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            if (savedInstanceState.keySet().contains(LOCATION_KEY))
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);


        }

    }





    public void deviceUniqueNumber() {
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Toast.makeText(this, deviceId + " " + getWifiMacAddress(), Toast.LENGTH_SHORT).show();
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

}