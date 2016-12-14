package com.example.gio.autostop.User_Interface.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;

import com.example.gio.autostop.Constants;
import com.example.gio.autostop.Server.DataRequestManager;
import com.example.gio.autostop.R;
import com.example.gio.autostop.User_Interface.fragments.DriverFragment;
import com.example.gio.autostop.User_Interface.fragments.MapFunctionsFragment;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks {


    public final static int MILISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    public final static int MINUTE = 60 * MILISECONDS_PER_SECOND;
    public int permissionRequestCounter;
    public GoogleApiClient mGoogleApiClient;
    public Boolean startedLocationUpdate;
    public LocationRequest locationRequest;
    public Location mCurrentLocation;
    public com.example.gio.autostop.User_Interface.fragments.AddressFragment AddressFragment;
    public MapFunctionsFragment mapFunctions;
    public DriverFragment driverFragment;
    public GoogleMap mMap;// Might be null if Google Play services APK is not available.
    private String mLastUpdateTime;
    private boolean mChoosedMode;
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private static final String TAG = "main-activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        mChoosedMode=intent.getBooleanExtra(Constants.chosenMode,false);

        setUpMapIfNeeded();
        AddressFragment = (com.example.gio.autostop.User_Interface.fragments.AddressFragment) getSupportFragmentManager().findFragmentById(R.id.AddressFragment);
        AddressFragment.setMapsActivity(this);
        mapFunctions=new MapFunctionsFragment();
        mapFunctions.setMapsActivity(this);
        driverFragment=new DriverFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(!mChoosedMode)
            ft.replace(R.id.placeholder,mapFunctions);
        else
            ft.replace(R.id.placeholder,driverFragment);
        ft.commit();
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
            if (AddressFragment.AddressRequested) {
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, startedLocationUpdate);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() && startedLocationUpdate)
            stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        DataRequestManager.getInstance().setUpMap(this, mapFunctions.callback);
//        mapFunctions.setUpMap();
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

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        AddressFragment.fetchAddressHandler();
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


    public void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        startedLocationUpdate = false;
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
            if (mMap != null) {
//                mapFunctions.setUpMap();
                DataRequestManager.getInstance().setUpMap(this, mapFunctions.callback);
            }
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
                startedLocationUpdate = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            if (savedInstanceState.keySet().contains(LOCATION_KEY))
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
        }

    }


}