package com.example.gio.autostop;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.text.DateFormat;
import java.util.ArrayList;
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
    public LocationManager mLocationManager;
    public final static int MILISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    public final static int MINUTE = 60 * MILISECONDS_PER_SECOND;
    protected String mLastUpdateTime;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected static final String TAG = "main-activity";
    public AddressFragment AddressFragment;


    private GoogleMap mMap;// Might be null if Google Play services APK is not available.

    private Button checkInButton,checkOutButton;
    private ArrayList<Marker> markerCollection = new ArrayList<>();
    private Marker  markerForDeletion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        checkInButton = (Button) findViewById(R.id.button2);
        checkOutButton=(Button)findViewById(R.id.button3);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(savedInstanceState==null){
            AddressFragment addressFragment1=new AddressFragment();
            addressFragment1.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,addressFragment1).commit();
        }
        AddressFragment =(AddressFragment)getSupportFragmentManager().findFragmentById(R.id.AddressFragment);
        AddressFragment.setMapsActivity(this);
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
        setUpMapIfNeeded();
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
//            AddressFragment gettingAddressFragment=(AddressFragment)getSupportFragmentManager().findFragmentById(R.id.map);
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
        mGoogleApiClient.disconnect();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
//                    .getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
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

    private void setUpMap() {
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
                                markerCollection.add(mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))));
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
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
        RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
        queue.add(downloadPosition);


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
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, AddressFragment.mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, AddressFragment.mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
                startedLocationUpdate = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            if (savedInstanceState.keySet().contains(LOCATION_KEY))
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                AddressFragment.mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                AddressFragment.mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                AddressFragment.displayAddressOutput();
            }

        }

    }






    public void checkInCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location location;
        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            location=locationGPS;
        }
        else {
            location=locationNet;
        }
        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerForDeletion=mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Positions position=new Positions(newLatLng.latitude,newLatLng.longitude,getWifiMacAddress(),deviceId);
        Response.Listener<String> responseListener= new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse= new JSONObject(s);
                    boolean success=jsonResponse.getBoolean("success");
                    if(!success){
                        AlertDialog.Builder builder=new AlertDialog.Builder(MapsActivity.this);
                        builder.setMessage("uploading position failed")
                        .setNegativeButton("retry",null)
                        .create()
                        .show();
                         }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        UploadPosition upload=new UploadPosition(position,responseListener);
        RequestQueue queue= Volley.newRequestQueue(MapsActivity.this);
        queue.add(upload);

    }
    public void deletePosition(){
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String mac=getWifiMacAddress();
        Response.Listener<String> responseListener = new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject jsonResponse= new JSONObject(s);
                    boolean success=jsonResponse.getBoolean("success");
                    if(!success){
                        AlertDialog.Builder builder=new AlertDialog.Builder(MapsActivity.this);
                        builder.setMessage("uploading position failed")
                                .setNegativeButton("retry",null)
                                .create()
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        DeletePosition delete=new DeletePosition(mac,deviceId,responseListener);
        RequestQueue queue=Volley.newRequestQueue(MapsActivity.this);
        queue.add(delete);
    }


    public void deviceUniqueNumber(){
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Toast.makeText(this,deviceId+" "+getWifiMacAddress(),Toast.LENGTH_SHORT).show();
    }
        public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null){
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            Log.i("getWifiMacAddress","exception in getWifiMacAddress");
        }
        return "";
    }

}