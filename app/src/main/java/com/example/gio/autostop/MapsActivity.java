package com.example.gio.autostop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;



public class MapsActivity extends FragmentActivity implements LocationListener,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,GoogleApiClient.ConnectionCallbacks{

    private GoogleMap mMap;// Might be null if Google Play services APK is not available.
    public int permissionRequestCounter =0;
    public GoogleApiClient mGoogleApiClient;
    public Boolean mRequestingLocationUpdates;
    public LocationRequest locationRequest;
    public final static int MILISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    public final static int MINUTE = 60 * MILISECONDS_PER_SECOND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mRequestingLocationUpdates=false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILISECONDS_PER_SECOND);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
//        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates)
//            requestLocationUpdates();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mRequestingLocationUpdates)
        requestLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
        stopLocationUpdates();

    }

    private void requestLocationUpdates() {
        // Should we show an explanation?
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(MapsActivity.this, "Location access is required to display your location", Toast.LENGTH_SHORT).show();
            // aq shemodis tu motxovnaze uari tqva momxmarebelma magram never ask me again ar monishna
            if(permissionRequestCounter==0){//amis gareshe meorejer da shemdeg motxovnebze tu momxmarebeli
                // never ask me again-is gareshe monishnavda deny-s motxovna icikleboda

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                permissionRequestCounter++;
            }
        } else {
            // aq shemodis pirvelad motxovnisas da aseve motxovnis uaryopis shemtvevashi tu momxmarebelma tan never ask again monishna
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
        mRequestingLocationUpdates=true;


    }

    private void requestLocation(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, MapsActivity.this);
            } else
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }
    private void enableMyLocation() {
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
                if (grantResults.length==1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();

                } else {
                    Toast.makeText(MapsActivity.this, "Permission was blocked", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void setUpMap() {
    }

}
