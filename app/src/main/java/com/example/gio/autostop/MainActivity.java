package com.example.gio.autostop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
        public Button button1;
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest LocationRequest;
    public int permissionRequestCounter =0;
    public FusedLocationProviderApi LocationProvider = LocationServices.FusedLocationApi;
    public final static int MILISECONDS_PER_SECOND = 1000;
    public final static int REQUEST_FINE_LOCATION = 0;
    //    public final static int REQUEST_COARSE_LOCATION =1;
    public final static int MINUTE = 60 * MILISECONDS_PER_SECOND;
    public Boolean mRequestingLocationUpdates;
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates==false)
            requestLocationUpdates();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRequestingLocationUpdates=false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(MINUTE);
        LocationRequest.setFastestInterval(15 * MILISECONDS_PER_SECOND);
        LocationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           displayMap(v);
                                       }
                                   }
        );

    }

    public void displayMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        startActivity(intent);
        else
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }
    private void requestLocation(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, LocationRequest, MainActivity.this);
            } else
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        }
    }

    private void requestLocationUpdates() {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MainActivity.this, "Location access is required to display your location", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length==1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();

                } else {
                    Toast.makeText(MainActivity.this, "Permission was blocked", Toast.LENGTH_SHORT).show();

                }
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this,"Location Changed; "+location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_SHORT).show();

    }
}
