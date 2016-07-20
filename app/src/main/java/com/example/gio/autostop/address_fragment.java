package com.example.gio.autostop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


public class address_fragment extends Fragment {
    protected TextView mLocationAddressTextView;
    protected String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private ProgressBar mProgressBar;
    protected boolean mAddressRequested;
    private  MapsActivity mapsActivity;
    public address_fragment() {
        // Required empty public constructor
    }

    class AddressResultReceiver extends ResultReceiver{
        private int CREATOR;
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            mAddressRequested = false;
            updateUIWidgets();
            super.onReceiveResult(resultCode, resultData);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_address_fragment, container, false);
    }
    public void setMapsActivity(MapsActivity mapsActivity){
        this.mapsActivity=mapsActivity;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("mAddressOutput",mAddressOutput);
        outState.putBoolean("mAddressRequested",mAddressRequested);

    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if(view!=null){
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            mLocationAddressTextView=(TextView) view.findViewById(R.id.address);
            displayAddressOutput();
        }
    }
    public void displayAddressOutput(){
    mLocationAddressTextView.setText(mAddressOutput);
}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        mAddressOutput = " ";
        updateUIWidgets();
    }
    public void startIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mapsActivity.mCurrentLocation);
        mapsActivity.startService(intent);

    }
    public void fetchAddressHandler() {
        if (mapsActivity.mGoogleApiClient.isConnected() && mapsActivity.mCurrentLocation != null) {
            startIntentService();
        }
        mAddressRequested = true;
        updateUIWidgets();
    }


    private void updateUIWidgets() {

        if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }
}
