package com.example.gio.autostop.User_Interface.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;

import com.example.gio.autostop.Constants;
import com.example.gio.autostop.Server.DataRequestManager;
import com.example.gio.autostop.R;
import com.example.gio.autostop.Server.Positions;
import com.example.gio.autostop.Settings;
import com.example.gio.autostop.User_Interface.fragments.AddressFragment;
import com.example.gio.autostop.User_Interface.fragments.DriverFragment;
import com.example.gio.autostop.User_Interface.fragments.MapFunctionsFragment;
import com.example.gio.autostop.User_Interface.services.FetchAddressIntentService;
import com.example.gio.autostop.User_Interface.services.JSONParser;
import com.google.android.gms.location.LocationListener;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter,GoogleMap.OnMapClickListener {

    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;
    public int permissionRequestCounter;
    public GoogleApiClient mGoogleApiClient;
    public Boolean startedLocationUpdate;
    public LocationRequest locationRequest;
    public Marker markerForDeletionDestination;
    public Location mCurrentLocation;
    public AddressFragment AddressFragment;
    public MapFunctionsFragment mapFunctions;
    public DriverFragment driverFragment;
    private TextView mLocationAddressTextView;
    public GoogleMap mMap;// Might be null if Google Play services APK is not available.
    private String mLastUpdateTime;
    private String mAddressOutput;
    private Boolean mChosenMode;
    private View markerClickView;
    private List<Polyline> polyLines = new ArrayList<Polyline>();
    Polyline tempPolyline;
    private String url;
    private List<Marker> markerList = new ArrayList<Marker>();
    private AddressResultReceiver mResultReceiver;
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private static final String TAG = "main-activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        mChosenMode = intent.getBooleanExtra(Constants.chosenMode, false);

        setUpMapIfNeeded();
        mapFunctions = new MapFunctionsFragment();
        mapFunctions.setMapsActivity(this);
        driverFragment = new DriverFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!mChosenMode)
            ft.replace(R.id.placeholder, mapFunctions);
        else
            ft.replace(R.id.placeholder, driverFragment);
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
        locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Settings.checkGps(this);
        }
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressOutput = " ";
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
//        DataRequestManager.getInstance().setUpMap(this, mapFunctions.callback);
//        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
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
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnMapClickListener(this);
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
                    Settings.checkGps(this);
                } else {
                    Toast.makeText(MapsActivity.this, "Permission was blocked", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Settings.checkGps(this);
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
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

    @Override
    public void onMapLongClick(LatLng latLng) {
        if ((MapFunctionsFragment.isMarkerForDeletionInitialized() && !mChosenMode) || mChosenMode) {
            if (markerForDeletionDestination != null)
                markerForDeletionDestination.remove();
            markerForDeletionDestination = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            LatLng position = markerForDeletionDestination.getPosition();
            MapFunctionsFragment.setMarkerForDeletionDestination(markerForDeletionDestination);
            MapFunctionsFragment.uploadingPosition(position, mChosenMode);
            com.example.gio.autostop.Settings.saveBoolean("mCheckOutForDriverButton", true);
            if (mChosenMode)
                DriverFragment.unCheckDriver.setClickable(true);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        if (mGoogleApiClient.isConnected() && mCurrentLocation != null) {
//            startIntentService(marker); teqstis minichebamde xdeba panjris gamosvla
//        }
//        marker.showInfoWindow();

        if (markerList.contains(marker)) {
            tempPolyline = (Polyline) marker.getTag();
            tempPolyline.remove();
            markerList.remove(markerList.indexOf(marker));
        } else {
            if(MapFunctionsFragment.position!=null){
            LatLng latLng = marker.getPosition();
            Positions position = DataRequestManager.searchList(latLng.latitude, latLng.longitude);
            if (position != null)
                url = makeURL(position.getLatitude(), position.getLongitude(), position.getLatitudeDestination(), position.getLongitudeDestination());
            else
                url = makeURL(MapFunctionsFragment.position.getLatitude(), MapFunctionsFragment.position.getLongitude(), MapFunctionsFragment.position.getLatitudeDestination(), MapFunctionsFragment.position.getLongitudeDestination());
            new connectAsyncTask(url, marker).execute();
            }
        }
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        markerClickView = getLayoutInflater().inflate(R.layout.marker_text_layout, null);

        return markerClickView;
    }

    public void startIntentService(Marker marker) {
        LatLng latLng = marker.getPosition();
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, loc);
        startService(intent);

    }

    public String makeURL(double sourceLat, double sourceLog, double destLat, double destLog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");
        urlString.append(Double.toString(sourceLat));
        urlString.append(",");
        urlString.append(Double.toString(sourceLog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destLat));
        urlString.append(",");
        urlString.append(Double.toString(destLog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyDRHhjUg53FwxJqYtCjVs_uGl88ajCSUeo");
        return urlString.toString();
    }

    public void drawPath(String result, Marker marker) {
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
            marker.setTag(line);
            markerList.add(marker);
            polyLines.add(line);
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        } catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        for(Polyline line1 : polyLines)
        {
            line1.remove();
        }

        polyLines.clear();
    }

    //Receiver for data sent from FetchAddressIntentService.
    class AddressResultReceiver extends ResultReceiver {
        private int CREATOR;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            mLocationAddressTextView = (TextView) markerClickView.findViewById(R.id.address1);
            mLocationAddressTextView.setText(mAddressOutput);
        }
    }

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;
        Marker marker;

        connectAsyncTask(String urlPass, Marker marker) {
            this.marker = marker;
            this.url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (result != null) {
                drawPath(result, marker);
            }
        }
    }
}