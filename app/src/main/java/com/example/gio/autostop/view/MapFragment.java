package com.example.gio.autostop.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gio.autostop.MVP_Interfaces;
import com.example.gio.autostop.helper.AutostopSettings;
import com.example.gio.autostop.helper.Constants;
import com.example.gio.autostop.R;
import com.example.gio.autostop.helper.StateMaintainer;
import com.example.gio.autostop.model.MapModel;
import com.example.gio.autostop.presenter.MapPresenter;
import com.example.gio.autostop.server.Positions;
import com.example.gio.autostop.helper.GPSManager;
import com.example.gio.autostop.interfaces.GPSCallback;
import com.example.gio.autostop.interfaces.MapRequestRequestCallback;
import com.example.gio.autostop.services.FetchAddressIntentService;
import com.example.gio.autostop.services.JSONParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnMapClickListener, MVP_Interfaces.RequiredViewOps, MainFragment.onChooseModeListener {
    private GoogleMap mMap;// Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private CheckBox doNotShowAgain;
    private Boolean startedLocationUpdate;
    private Location mCurrentLocation;
    private final int REQUEST_FINE_LOCATION = 0;
    private LocationRequest locationRequest;
    private final String TAG = "main-fragment";
    private int permissionRequestCounter;
    private String mLastUpdateTime;
    private ProgressDialog progressDialog;
    private List<Polyline> polyLines = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private AddressFragment AddressFragment;
    private View markerClickView;
    private Polyline tempPolyline;
    private String url;
    private TextView mLocationAddressTextView;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private Boolean mChosenMode;
    private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final String LOCATION_KEY = "location-key";
    private final int MILLISECONDS_PER_SECOND = 1000;
    private final int MINUTE = 60 * MILLISECONDS_PER_SECOND;
    private AlertDialog alert;
    private GPSManager gpsManager = null;
    private Button mCheckOutButton;
    private Marker markerForDeletion, markerForDeletionDestination, markerVariable;
    private int measurement_index = Constants.INDEX_KM;
    private double speed = 0.0;
    private LatLng tempDestinationPosition, tempLatLng;
    private Double lastSpeed = 0.0;
    public ArrayList<Marker> mMarkerCollection = new ArrayList<>();
    private String myMac, deviceId;
    private LocationManager locationManager;
    private MVP_Interfaces.ProvidedPresenterOps mPresenter;
    private FragmentActivity myContext;
    StateMaintainer mStateMaintainer;
    private MapPresenter presenter;
    private MapModel model;

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMapIfNeeded();
        showDestinationAlertDialog();
//        getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        mCheckOutButton = (Button) view.findViewById(R.id.checkout);
        mCheckOutButton.setClickable(AutostopSettings.getBoolean("mCheckOutButton"));
        mCheckOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckOutButton.setClickable(false);
                AutostopSettings.saveBoolean("mCheckOutButton", false);
                mPresenter.notifyToDeleteMarkers(markerForDeletion, getActivity());
                gpsManagerRemove(gpsManager);
                if (mChosenMode)
                    AutostopSettings.saveBoolean("carIconAlreadyCreated", false);
                else
                    AutostopSettings.saveBoolean("passengerIconAlreadyCreated", false);
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupMVP();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mChosenMode = bundle.getBoolean(Constants.chosenMode);
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            startedLocationUpdate = false;
            permissionRequestCounter = 0;
            locationRequest = new LocationRequest();
            locationRequest.setInterval(MINUTE);
            locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
            locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                AutostopSettings.checkGps(getContext());
            }
            mResultReceiver = new AddressResultReceiver(new Handler());
            mAddressOutput = " ";
            updateValuesFromBundle(savedInstanceState);
            return inflater.inflate(R.layout.fragment_map_view, container, false);
        }
        return null;
    }


    public MapRequestRequestCallback callback = new MapRequestRequestCallback() {
        @Override
        public void onRequestedLoaded(double lat, double lon, String mac, String android_id, double latitudeDestination, double longitudeDestination, Boolean kindOfUser) {
            deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            myMac = mPresenter.getWifiAddress();
            if (!isMarkerOnArray(mMarkerCollection, lat, lon) || (myMac.equals(mac) && deviceId.equals(android_id)))
                if (myMac.equals(mac) && deviceId.equals(android_id)) {
                    markerForDeletion = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
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
                    markerVariable = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
                    if (!kindOfUser)
                        markerVariable.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                    else
                        markerVariable.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                    mMarkerCollection.add(markerVariable);
                }
        }
    };

    public void dropMarkerOnMap(LatLng newLatLng) {
        if (!mChosenMode) {
            if (!AutostopSettings.getBoolean("passengerIconAlreadyCreated")) {
                markerForDeletion = mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
                markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.passenger));
                AutostopSettings.saveBoolean("passengerIconAlreadyCreated", false);
            }
        } else {
            if (!AutostopSettings.getBoolean("carIconAlreadyCreated")) {
                markerForDeletion = mMap.addMarker(new MarkerOptions().position(newLatLng).title(newLatLng.toString()));
                markerForDeletion.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                AutostopSettings.saveBoolean("carIconAlreadyCreated", false);
            }
        }
    }


    @Override
    public void notifyDeletedMarkers() {
        if (markerForDeletionDestination != null)
            markerForDeletionDestination.remove();
        if (!mChosenMode) {
            AutostopSettings.saveBoolean("mCheckOutButton", false);
        } else {
            AutostopSettings.saveBoolean("mCheckOutForDriverButton", false);
            mCheckOutButton.setClickable(false);
        }

    }


    @Override
    public Location notifyGetLastKnownLocation(Context context) {
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

    @Override
    public void gpsManagerStart(LatLng destinationPosition, final Boolean chosenMode) {
        gpsManager = new GPSManager();
        gpsManager.startListening(getContext(), mGoogleApiClient);
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
                        dropMarkerOnMap(mPresenter.checkInCurrentPosition(getContext()));
                        mPresenter.uploadingPosition(getActivity(), tempDestinationPosition, chosenMode);
                        Toast.makeText(getContext(), "driver " + speedDouble.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getContext(), "passenger is in car" + speedDouble.toString(), Toast.LENGTH_SHORT).show();

                } else {//pexit mgzavroba
                    if (lastSpeed > 0.0) {
                        checkInReminderAlertMessage(getContext());
                        Toast.makeText(getContext(), "passenger is not in car  " + speedDouble.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    public void checkInReminderAlertMessage(Context context) {
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

    public void showDestinationAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater DialogInflater = LayoutInflater.from(getActivity());
        View messageLayout = DialogInflater.inflate(R.layout.checkbox, null);
        doNotShowAgain = (CheckBox) messageLayout.findViewById(R.id.skip);
        builder.setMessage(R.string.destination)
                .setTitle(R.string.dialog_title)
                .setView(messageLayout);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String checkBoxResult = "NOT checked";
                if (doNotShowAgain.isChecked()) checkBoxResult = "checked";
                AutostopSettings.saveString("skipMessage", checkBoxResult);
                dialog.cancel();
            }
        });
        String skipMessage = AutostopSettings.getString("skipMessage");
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//prevent to get dismissed with back key
        dialog.setCanceledOnTouchOutside(false);//prevent to get dismissed on outside click
        if (!skipMessage.equals("checked"))
            dialog.show();
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

        } catch (JSONException e) {

        }
    }

    public void setUpMapIfNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    public void enableMyLocation() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                if (permissionRequestCounter == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    }

                    permissionRequestCounter++;
                }
            } else if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
            }

        }
    }


    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }

    private double convertSpeed(double speed) {
        return ((speed * Constants.HOUR_MULTIPLIER) * Constants.UNIT_MULTIPLIERS[measurement_index]);
    }


    public boolean isMarkerForDeletionInitialized() {
        return markerForDeletion != null;
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    AutostopSettings.checkGps(getContext());
                } else {
                    Toast.makeText(getContext(), "Permission was blocked", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!startedLocationUpdate)
            startLocationUpdates();
        if (mCurrentLocation != null) {

            if (!Geocoder.isPresent()) {
                Toast.makeText(getContext(), R.string.no_geocoder_available, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !startedLocationUpdate)
            startLocationUpdates();
        mCheckOutButton.setClickable(AutostopSettings.getBoolean("mCheckOutButton"));

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
//        ((Activity) getContext()).overridePendingTransition(R.anim.activity_close_translate, R.anim.activity_open_scale);
        if (mGoogleApiClient.isConnected() && startedLocationUpdate)
            stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        startedLocationUpdate = false;
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                startedLocationUpdate = true;
            } else {
                if (permissionRequestCounter == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    }
                    permissionRequestCounter++;
                }
            }
        }

    }

    public void gpsManagerRemove(GPSManager gpsManager) {
        if (gpsManager != null) {
            gpsManager.stopListening(mGoogleApiClient);
            gpsManager.setGPSCallback(null);
            gpsManager = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (mGoogleApiClient.isConnected())
            gpsManagerRemove(gpsManager);
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
        mPresenter.onDestroy(getActivity().isChangingConfigurations());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(this);
        mMap.setOnMapClickListener(this);
        enableMyLocation();
        mPresenter.setUpMapDemand(getActivity(), callback);
        if (markerForDeletion == null) {
            AutostopSettings.saveBoolean("carIconAlreadyCreated", false);
            AutostopSettings.saveBoolean("passengerIconAlreadyCreated", false);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        AutostopSettings.checkGps(getContext());
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        AutostopSettings.checkGps(getContext());
        final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            dropMarkerOnMap(mPresenter.checkInCurrentPosition(getContext()));
            mCheckOutButton.setClickable(true);
            dropDestinationMarker(latLng);
        }
    }

    public void dropDestinationMarker(LatLng latLng) {
        if (isMarkerForDeletionInitialized()) {
            if (markerForDeletionDestination != null)
                markerForDeletionDestination.remove();
            markerForDeletionDestination = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            LatLng position = markerForDeletionDestination.getPosition();
            mPresenter.uploadingPosition(getActivity(), position, mChosenMode);
            if (mChosenMode)
                mCheckOutButton.setClickable(true);
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
            LatLng latLng = marker.getPosition();
            Positions searchedPosition = mPresenter.searchList(latLng.latitude, latLng.longitude);
            if (searchedPosition != null) {
                url = makeURL(searchedPosition.getLatitude(), searchedPosition.getLongitude(), searchedPosition.getLatitudeDestination(), searchedPosition.getLongitudeDestination());
                new connectAsyncTask(url, marker).execute();
            } else
                return true;
        }
        return true;
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
        urlString.append("&key=AIzaSyAoustpvxTVV1xDwJ1ouCBE5ti2LU3WCzo");
        return urlString.toString();
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
//        markerClickView = getLayoutInflater(null).inflate(R.layout.marker_text_layout, null);
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        for (Polyline line1 : polyLines) {
            line1.remove();
        }

        polyLines.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, startedLocationUpdate);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
                startedLocationUpdate = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            if (savedInstanceState.keySet().contains(LOCATION_KEY))
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
        }
    }

    private void setupMVP() {
        mStateMaintainer = new StateMaintainer(myContext.getSupportFragmentManager(), MapFragment.class.getName());
        presenter = new MapPresenter(this);
        if (mStateMaintainer.firstTimeIn()) {
            model = new MapModel(presenter);
            presenter.setModel(model);
            // Add Presenter and Model to StateMaintainer
            mStateMaintainer.put(presenter);
            mStateMaintainer.put(model);
                    }
        // get the Presenter from StateMaintainer
        else {
            mPresenter = mStateMaintainer.get(MapPresenter.class.getName());
            // Updated the View in Presenter
            mPresenter.setView(this);
            model = mStateMaintainer.get(MapModel.class.getName());
            presenter.setModel(model);
        }
        // Set the Presenter as a interface
        // To limit the communication with it
        mPresenter = presenter;
    }

    public void startIntentService(Marker marker) {
        LatLng latLng = marker.getPosition();
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, loc);
        getActivity().startService(intent);
    }

    @Override
    public void notifyChosenMode(boolean chosen) {
        mChosenMode = chosen;
    }

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        String url;
        Marker marker;

        connectAsyncTask(String urlPass, Marker marker) {
            this.marker = marker;
            this.url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            progressDialog = null;
            if (result != null) {
                drawPath(result, marker);
            }
        }
    }

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
}
